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

package ch.hsr.ifs.cdt.metriculator.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLambdaExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;

import ch.hsr.ifs.cdt.metriculator.model.nodes.AbstractNode;
import ch.hsr.ifs.cdt.metriculator.model.nodes.CompositeTypeNode;
import ch.hsr.ifs.cdt.metriculator.model.nodes.FunctionNode;
import ch.hsr.ifs.cdt.metriculator.model.nodes.NamespaceNode;
import ch.hsr.ifs.cdt.metriculator.nodes.nodeInfo.FuncDeclNodeInfo;
import ch.hsr.ifs.cdt.metriculator.nodes.nodeInfo.FuncDefNodeInfo;

public class ScopedASTVisitor extends ASTVisitor {

	protected AbstractNode scopeNode;
	TreeBuilder builder;

	public ScopedASTVisitor(AbstractNode scopeNode, TreeBuilder builder) {
		this.scopeNode = scopeNode;
		this.builder   = builder;

		// overwrite super values to allow scoping
		shouldVisitNamespaces     = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitDeclarations   = true;
		shouldVisitStatements     = true;
		shouldVisitDeclarators    = true;
	}

	private Collection<IScopeListener> listeners = new ArrayList<IScopeListener>();
	public void add(IScopeListener listener) {
		listeners.add(listener);
	}

	@Override
	public int visit(ICPPASTNamespaceDefinition namespaceDefinition) {
		scopeNode = builder.addChild(scopeNode, new NamespaceNode(namespaceDefinition));
		return super.visit(namespaceDefinition);
	}			
	@Override
	public int leave(ICPPASTNamespaceDefinition namespaceDefinition) {
		scopeNode = scopeNode.getParent();
		return super.leave(namespaceDefinition);
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if(declaration instanceof ICPPASTFunctionDefinition){
			scopeNode = builder.addChild(scopeNode, new FunctionNode(((ICPPASTFunctionDefinition)declaration)));
		}
		return super.visit(declaration);

	}
	@Override
	public int leave(IASTDeclaration declaration) {
		if(declaration instanceof ICPPASTFunctionDefinition){
			for(IScopeListener listener : listeners){
				listener.leaving((FunctionNode) scopeNode);
			}
			scopeNode = scopeNode.getParent();
		}
		return super.leave(declaration);
	}

	/*
	 * TODO: write test for IASTImplicitNameOwner function declarators
	 */
	@Override
	public int visit(IASTDeclarator declarator) {
		if(declarator instanceof ICPPASTFunctionDeclarator && !(declarator.getParent() instanceof ICPPASTFunctionDefinition)){
			
			if(declarator.getChildren().length > 0 && (declarator.getChildren()[0] instanceof IASTImplicitNameOwner)){
				return PROCESS_SKIP;
			}
			
			if(!(scopeNode instanceof FunctionNode)){
				scopeNode = builder.addChild(scopeNode, new FunctionNode(((ICPPASTFunctionDeclarator)declarator)));
				if(!(scopeNode instanceof FunctionNode)){
					return PROCESS_SKIP;
				}
				return super.visit(declarator);
			}
			return PROCESS_SKIP;
		}
		
		return super.visit(declarator);
	}
	@Override
	public int leave(IASTDeclarator declarator) {
		if(declarator instanceof ICPPASTFunctionDeclarator){
			if((scopeNode.getNodeInfo() instanceof FuncDeclNodeInfo 
					|| scopeNode.getNodeInfo() instanceof FuncDefNodeInfo) 
					&& !(declarator.getParent() instanceof ICPPASTFunctionDefinition) 
					&& !(declarator.getParent() instanceof ICPPASTLambdaExpression)){
				
				for(IScopeListener listener : listeners){
					listener.leaving((FunctionNode) scopeNode);
				}
				scopeNode = scopeNode.getParent();
			}
		}
		
		return super.leave(declarator);
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {

		boolean scopeChanged = false;

		String oldScope = scopeNode.getScopeName();
		if(declSpec instanceof ICPPASTCompositeTypeSpecifier){
			scopeNode = builder.addChild(scopeNode, new CompositeTypeNode(((ICPPASTCompositeTypeSpecifier)declSpec)));
			if(oldScope.equals(scopeNode.getScopeName())){
				return PROCESS_SKIP;
			}
			scopeChanged = true;
		}
		if(declSpec instanceof ICPPASTElaboratedTypeSpecifier){
			scopeNode = builder.addChild(scopeNode, new CompositeTypeNode(((ICPPASTElaboratedTypeSpecifier)declSpec)));
			if(oldScope.equals(scopeNode.getScopeName())){
				return PROCESS_SKIP;
			}
			scopeChanged = true;
		}
		
		if(scopeChanged){
			for(IScopeListener listener : listeners){
				listener.visiting((CompositeTypeNode) scopeNode);
			}
		}

		return super.visit(declSpec);
	}	
	@Override
	public int leave(IASTDeclSpecifier declSpec) {
		if(declSpec instanceof ICPPASTCompositeTypeSpecifier){
			for(IScopeListener listener : listeners){
				listener.leaving((CompositeTypeNode) scopeNode);
			}
			scopeNode = scopeNode.getParent();
		}
		if(declSpec instanceof ICPPASTElaboratedTypeSpecifier){
			scopeNode = scopeNode.getParent();
		}
		return super.leave(declSpec);
	}
}
