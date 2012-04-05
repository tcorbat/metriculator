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

import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cdt.metriculator.resources.Icon;

public abstract class FunctionNode extends MemberNode {

	private static final String KEYWORD_FRIEND = "friend";

	public FunctionNode(String scopeUniqueName) {
		super(scopeUniqueName);
	}
	
	protected FunctionNode(String scopeUniqueName, IASTNode astNode) {
		super(scopeUniqueName, astNode);
	}

	@Override
	protected void prepareIsFriend(IASTNode astNode) {
		isFriend = astNode.getRawSignature().contains(KEYWORD_FRIEND);
	}

	@Override
	public String getIconPath() {
		return Icon.Size16.METHOD_PUBLIC;
	}
	
}
