import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Vector;

public class SymbolTableVisitor extends CLangDefaultVisitor {

    Vector<String> _data;
    Vector<String> _text;
    int stackIndex = 0;

    public static class SymbolTableEntry {
        public String name;
        public String type;
        public int offset;

        public SymbolTableEntry(String name, String type, int offset)
        {
            this.name = name;
            this.type = type;
            this.offset = offset;
        }
    }

    HashMap<String, SymbolTableEntry> symbols = new HashMap<>();

    public SymbolTableVisitor() {
        this._text = new Vector<>();
        this._data = new Vector<>();

    }

    public SymbolTableEntry resolve(String s) {
        return symbols.get(s);
    }

    public void put(SymbolTableEntry s)
    {
        symbols.put(s.name, s);
    }

    @Override
    public Object visit(ASTparamDef node, Object data) {
        Object res = super.visit(node, data);

        SymbolTableEntry e = new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex);
        this.stackIndex += 4;
        put(e);
        
        return res;
    }

    @Override
    public Object visit(ASTvarDefineDef node, Object data) {

        boolean isInt = node.firstToken.image.equals("int");
        if (isInt)
            this.stackIndex+=4;
        else
            this.stackIndex++;

        SymbolTableEntry e = new SymbolTableEntry(node.firstToken.next.image, node.firstToken.image, this.stackIndex);
        
        if (node.children.length > 0)
        {

            data = node.children[0].jjtAccept(this, data);
            _text.add("pop rax");
            _text.add(String.format("mov %s [rbp - %d], %s", isInt ? "dword" : "byte", e.offset, isInt ? "eax" : "al"));
        }

        put(e);
        
        return data;
    }

    @Override
    public Object visit(ASTunaryExpressionDef node, Object data) {
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTaddExpressionDef node, Object data) {
        data = node.children[0].jjtAccept(this, data);
        if (node.children.length > 1)
        {
            data = node.children[1].jjtAccept(this, data);
            if (data !=null){
            _text.add("pop rbx");
            _text.add("pop rax");
            _text.add("add rax, rbx");
            _text.add("push rax");
           }
        }
        
        return data;
    }
    
    @Override
    public Object visit(ASTconstExpressionDef node, Object data) {
        
        if (node.firstToken.kind == CLang.ID)
        {
            SymbolTableEntry e = resolve(node.firstToken.image);
            if (e == null)
            {
                System.err.println(String.format("Variable %s is not defined at %d : %d",
                                                    node.firstToken.image,
                                                    node.firstToken.beginLine,
                                                    node.firstToken.beginColumn));
                System.exit(-1);
            }

            boolean isInt = e.type.equals("int");

            _text.add(String.format("mov %s, %s [rbp - %d]", isInt ? "eax" : "al", isInt ? "dword" : "byte", e.offset));
            _text.add("push rax");

        }

        if (node.firstToken.kind == CLang.NUMBER)
        {
            _text.add(String.format("mov rax, %s", node.firstToken.image));
            _text.add("push rax");
        }

        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTfunctionDef node, Object data) {
        _text.add(String.format("%s:" ,node.firstToken.next.image));
        _text.add("push rbp");
        _text.add("mov rbp, rsp");
        Object o = super.visit(node, data);
        _text.add("mov rsp, rbp");
        _text.add("pop rbp");
        _text.add("ret");
        return o;
        // return super.visit(node, data);
    }

    @Override
    public Object visit(ASTStart node, Object data) {
        Object o = super.visit(node, data);

        System.out.println("SECTION .TEXT\n" +
        "GLOBAL main\n" +
        "\n" +
        "printChar:\n" +
        "    push rbp\n" +
        "    mov rbp, rsp\n" +
        "    push rdi\n" +
        "    mov byte [rbp - 5], 0x41\n" +
        "    mov byte [rbp - 4], 0x53\n" +
        "    mov byte [rbp - 3], 0x41\n" +
        "    mov byte [rbp - 2], 0x46\n" +
        "    mov byte [rbp - 1], 0\n" +
        "    mov rax, 1\n" +
        "    mov rdi, 1\n" +
        "    lea rsi, [rbp -5]\n" +
        "    mov rdx, 5\n" +
        "    syscall \n" +
        "\n" +
        "    mov rsp, rbp\n" +
        "    pop rbp\n" +
        "    ret\n" +
        "\n" +
        "printNumber:\n" +
        "    push rbp\n" +
        "    mov rbp, rsp\n" +
        "    mov rsi, rdi\n" +
        "    lea rdi, [rbp - 1]\n" +
        "    mov byte [rdi], 0\n" +
        "    mov rax, rsi\n" +
        "    while:\n" +
        "    cmp rax, 0\n" +
        "    je done\n" +
        "    mov rcx, 10\n" +
        "    mov rdx, 0\n" +
        "    div rcx\n" +
        "    dec rdi\n" +
        "    add dl, 0x30\n" +
        "    mov byte [rdi], dl\n" +
        "    jmp while\n" +
        "\n" +
        "    done:\n" +
        "        mov rax, 1\n" +
        "        lea rsi, [rdi]\n" +
        "        mov rdx, rsp\n" +
        "        sub rdx, rsi\n" +
        "        mov rdi, 1\n" +
        "        syscall \n" +
        "\n" +
        "        mov rsp, rbp\n" +
        "        pop rbp\n" +
        "        ret\n" +
        "\n" +
        "readInteger:\n" +
        "    push rbp\n" +
        "    mov rbp, rsp\n" +
        "\n" +
        "    mov rdx, 10\n" +
        "    mov qword [rbp - 10], 0\n" +
        "    mov word [rbp - 2], 0\n" +
        "    lea rsi, [rbp - 10]\n" +
        "    mov rdi, 0 ; stdin\n" +
        "    mov rax, 0 ; sys_read\n" +
        "    syscall\n" +
        "\n" +
        "    xor rax, rax\n" +
        "    xor rbx, rbx\n" +
        "    lea rcx, [rbp - 10]\n" +
        "    \n" +
        "    copy_byte:\n" +
        "        cmp rbx, 10\n" +
        "        je read_done    \n" +
        "        mov dl, byte [rcx]\n" +
        "        cmp dl, 10\n" +
        "        jle read_done\n" +
        "        sub rdx, 0x30\n" +
        "        imul rax, 10\n" +
        "        add rax, rdx\n" +
        "        nextchar:\n" +
        "            inc rcx\n" +
        "            inc rbx\n" +
        "            jmp copy_byte\n" +
        "    read_done:\n" +
        "        mov rsp, rbp\n" +
        "        pop rbp\n" +
        "        ret\n" +
        "\n");
        for (String s : _text)
            System.out.println(s);
        return o;
    }

    
    public static void main(String[] args) throws FileNotFoundException, ParseException {
        new CLang(new FileInputStream(args[0]));

        CLang.Start();

        System.out.println("Parsing succeeded, begin compiling");
        

        CLang.jjtree.rootNode().jjtAccept(new SymbolTableVisitor(), null);
    }
}