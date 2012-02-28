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

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IFile;


public class NodeInfo {

	private boolean isFunctionDeclarator = false;
	private boolean isFunctionDefinition = false;
	private boolean isElaboratedTypeSpecifier = false;
	private boolean isCompositeTypeSpecifier = false;
	private boolean isHeaderUnit = false;
	private boolean hasInfos = true;
	private IBinding indexBinding;
	private IBinding typeBinding;
	private String filePath;
	private int nodeOffSet;
	private int nodeLength;
	private int typeKey;
	protected String astNodeHashCode;
	private int endingLineNumber;
	private int startingLineNumber;
	private int nodeOffSetStart;
	private int nodeOffsetEnd;
	private boolean isEclosedInMacroExpansion;
	private boolean isFriend;

	public NodeInfo(){
		hasInfos = false;
	}

	public NodeInfo(IASTNode astNode) {
		// junit tests provide no astNode
		if(astNode != null){
			astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
		}
	}

	public NodeInfo(ICPPASTCompositeTypeSpecifier astNode){
		isCompositeTypeSpecifier = true;
		typeKey = astNode.getKey();
		astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
		prepareFilePath(astNode);
		prepareNodeLocations(astNode);
		prepareBindingFor(astNode.getTranslationUnit(), astNode.getName());
		prepareProblemLocation(astNode);
		isFriend = ((ICPPASTDeclSpecifier)astNode).isFriend();
	}

	public NodeInfo(ICPPASTElaboratedTypeSpecifier astNode) {
		isElaboratedTypeSpecifier = true;
		typeKey = astNode.getKind();
		astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
		prepareFilePath(astNode);
		prepareNodeLocations(astNode);
		prepareBindingFor(astNode.getTranslationUnit(), astNode.getName());
		prepareProblemLocation(astNode);
		isFriend = ((ICPPASTDeclSpecifier)astNode).isFriend();
	}

	public NodeInfo(IASTTranslationUnit astNode){
		// junit tests provide no astNode
		if(astNode != null){
			isHeaderUnit = astNode.isHeaderUnit();
			astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
			prepareFilePath(astNode);
			prepareNodeLocations(astNode);
			prepareProblemLocation(astNode);
		}
	}

	public NodeInfo(ICPPASTFunctionDefinition astNode){
		prepareFilePath(astNode);
		astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
		isFunctionDefinition = true;
		prepareBindingFor(astNode.getTranslationUnit(), astNode.getDeclarator());
		prepareNodeLocations(astNode);
		prepareProblemLocation(astNode);
		isFriend = astNode.getDeclSpecifier().getRawSignature().contains("friend");
	}

	public NodeInfo(ICPPASTFunctionDeclarator astNode){
		prepareFilePath(astNode);
		astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
		isFunctionDeclarator = true;
		prepareBindingFor(astNode.getTranslationUnit(), astNode);
		prepareNodeLocations(astNode);
		prepareProblemLocation(astNode);
		isFriend = astNode.getRawSignature().contains("friend");
	}

	public NodeInfo(ICPPASTNamespaceDefinition astNode){
		astNodeHashCode = Integer.valueOf(astNode.hashCode()).toString();
		prepareFilePath(astNode);
		prepareNodeLocations(astNode);
	}

	public boolean isFunctionDeclarator() {
		return isFunctionDeclarator;
	}

	public boolean isFunctionDefinition() {
		return isFunctionDefinition;
	}

	public IBinding getBinding(){
		return indexBinding;
	}

	public String getFilePath(){
		return filePath;
	}

	public int getNodeOffset(){
		return nodeOffSet;
	}

	public int getNodeLength(){
		return nodeLength;
	}

	public boolean isElaboratedTypeSpecifier() {
		return isElaboratedTypeSpecifier;
	}

	public boolean isCompositeTypeSpecifier() {
		return isCompositeTypeSpecifier;
	}
	
	public boolean isFriend(){
		return isFriend;
	}

	public int getTypeKey() {
		return typeKey;
	}

	private void prepareFilePath(IASTNode node){
		filePath = node.getTranslationUnit().getFilePath();
	}

	private void prepareBindingFor(IASTTranslationUnit tu, IASTFunctionDeclarator declarator) {
		prepareBindingFor(tu, declarator.getName());
	}

    private void prepareBindingFor(IASTTranslationUnit tu, IASTName name) {
        typeBinding  = name.resolveBinding();
        IIndex index = tu.getIndex();
        indexBinding = index.adaptBinding(typeBinding);
        
        if(indexBinding == null){
        	indexBinding = typeBinding;
        }
    }
    
    public static IBinding getBindingFor(IASTName name, IASTTranslationUnit tu){
    	IBinding typeBinding, indexBinding;
    	
        typeBinding  = name.resolveBinding();
        IIndex index = tu.getIndex();
        indexBinding = index.adaptBinding(typeBinding);
        
        return indexBinding == null ? typeBinding : indexBinding;
    }

	private void prepareNodeLocations(IASTNode astNode){
		nodeOffSet = astNode.getNodeLocations()[0].getNodeOffset();
		nodeLength = astNode.getNodeLocations()[0].getNodeLength();
	}


	public boolean isHeaderUnit() {
		return isHeaderUnit;
	}

	public boolean hasInfos() {
		return hasInfos;
	}

	public IBinding getTypeBinding(){
		return typeBinding;
	}
	
	public String getASTNodeHash() {
		return astNodeHashCode;
	}
	
	@Override
	public int hashCode() {
		  assert false : "hashCode not designed";
		  return 23;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!(obj instanceof NodeInfo)) return false;
		
		NodeInfo other = (NodeInfo) obj;
		boolean same = false;

		same = (hasInfos == other.hasInfos &&
				astNodeHashCode.equals(other.astNodeHashCode) &&
				isCompositeTypeSpecifier == other.isCompositeTypeSpecifier && 
				isElaboratedTypeSpecifier == other.isElaboratedTypeSpecifier &&
				isFunctionDeclarator == other.isFunctionDeclarator && 
				isFunctionDefinition == other.isFunctionDefinition && 
				isHeaderUnit == other.isHeaderUnit &&
				indexBinding != null ? indexBinding.equals(other.indexBinding) : other.indexBinding == null && 
				filePath.equals(other.filePath) && 
				typeKey == other.typeKey && 
				nodeLength == other.nodeLength && 
				nodeOffSet == other.nodeOffSet);

		return same;
	}
	
	public IProblemLocation createAndGetProblemLocation(IFile file) {
		IProblemLocationFactory locFactory = CodanRuntime.getInstance().getProblemLocationFactory();
		if(isEclosedInMacroExpansion || startingLineNumber == endingLineNumber){
			return locFactory.createProblemLocation(file, nodeOffSetStart, nodeOffsetEnd, startingLineNumber);
		}
		return locFactory.createProblemLocation(file, startingLineNumber);
	}

	private void prepareProblemLocation(IASTNode astNode){
		IASTFileLocation astLocation       = astNode.getFileLocation();
		
		startingLineNumber = astLocation.getStartingLineNumber();
		
		if (enclosedInMacroExpansion(astNode) && astNode instanceof IASTName) {
			isEclosedInMacroExpansion = true;
			IASTImageLocation imageLocation = ((IASTName) astNode).getImageLocation();
			
			if (imageLocation != null) {
				nodeOffSetStart = imageLocation.getNodeOffset();
				nodeOffsetEnd   = nodeOffSetStart + imageLocation.getNodeLength();
				return;
			}
		}
		
		endingLineNumber = astLocation.getEndingLineNumber();
		if (startingLineNumber == endingLineNumber) {
			nodeOffSetStart = astLocation.getNodeOffset();
			nodeOffsetEnd = nodeOffSetStart + astLocation.getNodeLength();
			return;
		}
		
	}

	private static boolean enclosedInMacroExpansion(IASTNode node) {
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
		return nodeLocations.length == 1 && nodeLocations[0] instanceof IASTMacroExpansionLocation;
	}
	
}