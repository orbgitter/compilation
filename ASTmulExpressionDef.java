/* Generated By:JJTree: Do not edit this line. ASTmulExpressionDef.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTmulExpressionDef extends SimpleNode {
  public ASTmulExpressionDef(int id) {
    super(id);
  }

  public ASTmulExpressionDef(CLang p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CLangVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=a4a835f2519cbe6597e60a8001cf78eb (do not edit this line) */
