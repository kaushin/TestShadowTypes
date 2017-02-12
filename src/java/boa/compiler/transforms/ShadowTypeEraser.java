/*
 * Copyright 2017, Hridesh Rajan, Robert Dyer, Kaushik Nimmala
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.compiler.transforms;

import java.util.*;

import boa.compiler.visitors.AbstractVisitorNoArg;

import boa.compiler.SymbolTable;

import boa.compiler.ast.Factor;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.Node;
import boa.compiler.ast.Component;
import boa.compiler.ast.Identifier;

import boa.compiler.ast.expressions.*;

import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.statements.Statement;

import boa.types.BoaShadowType;
import boa.types.proto.StatementProtoTuple;
import boa.types.BoaTuple;
/**
 * Converts a tree using shadow types into a tree without shadow types.
 *
 * @author rdyer
 * @author kaushin
 */
public class ShadowTypeEraser extends AbstractVisitorNoArg {

	
	private SymbolTable env;

/*	private HashMap<String,BoaTuple> tupleMap = new HashMap<String,BoaTuple>(); 
	private HashMap<String,String> tokenMap = new HashMap<String,String>(); 
	
	public ShadowTypeEraser(){
		tokenMap.put("IfStatement","Statement");
		tupleMap.put("Statement",new StatementProtoTuple());
	}
	*/		
	@Override
	public void visit(final Selector n) {
		//System.out.println("Selector ID = "+ n.getId());
		n.getId().accept(this);
	}

	
	
	@Override
	public void visit(final Component n) {
		if (n.hasIdentifier()){
			n.getIdentifier().accept(this);
		}
		//System.out.println("Component Type = "+ n.getType().toString());
		if(n.type instanceof BoaShadowType){
			System.out.println("Shadow Type before/after Found : " + n.getType());
		}
		n.getType().accept(this);
	}

	@Override
	public void visit(final VarDeclStatement n) {
		super.visit(n);
		//get Symbol Table
		env = n.env;
		if (n.hasType()){
			if(n.type instanceof BoaShadowType){
				//Change the Identifier in the ast
				BoaShadowType typeUsed = (BoaShadowType)env.get(n.getType().toString());
				Identifier temp = (Identifier)n.getType();
				temp.setToken(typeUsed.getDeclarationIdentifierEraser());
				System.out.println("Shadow Type Declaration Found = "+ temp.getToken());
				
				//Change the Type in the SymbolTable
				env.setType(n.getId().getToken(),typeUsed.getDeclarationSymbolTableEraser());
			}
		}
		
	}
}
