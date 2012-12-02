/******************************************************************************
* Copyright (c) 2011 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/

package ch.hsr.ifs.cdt.metriculator.model.nodes;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.cdt.metriculator.model.INodeVisitor;

public class FunctionDeclNode extends FunctionNode {

	public FunctionDeclNode(ICPPASTFunctionDeclarator fnNode) {
		super(fnNode.getRawSignature(), fnNode);
	}

	@Override
	public void accept(INodeVisitor v){
		v.visit(this);
	}
	
	@Override
	protected IASTName getASTName(IASTNode astNode) {
		return ((ICPPASTFunctionDeclarator)astNode).getName();
	}

}
