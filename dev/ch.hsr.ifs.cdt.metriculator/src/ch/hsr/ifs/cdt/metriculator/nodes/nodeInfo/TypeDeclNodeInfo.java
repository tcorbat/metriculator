package ch.hsr.ifs.cdt.metriculator.nodes.nodeInfo;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;



public class TypeDeclNodeInfo extends MemberNodeInfo {

	private int typeKey;
	
	protected TypeDeclNodeInfo(ICPPASTElaboratedTypeSpecifier astNode) {
		super(astNode);
		isFriend = ((ICPPASTDeclSpecifier)astNode).isFriend();
		typeKey = astNode.getKind();
	}
	
	public int getTypeKey() {
		return typeKey;
	}

	@Override
	protected void prepareBinding(IASTNode astNode) {
		IASTName name = ((ICPPASTElaboratedTypeSpecifier) astNode).getName();
		binding  = name.resolveBinding();
	}

}
