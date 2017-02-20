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

	@Override
	public void visit(final Factor n) {
		n.getOperand().accept(this);
		for (final Node o : n.getOps())
			o.accept(this);
	}

	@Override
	public void visit(final Selector n) {
		super.visit(n);

		env = n.env;
		//Get Term node and replace its child(I was thinking this is where I need to replace the tree)
		Term test =  (Term)n.getParent().getParent();
		Identifier callingID = (Identifier)test.getLhs().getOperand();
		if (env.get(callingID.getToken()) instanceof BoaShadowType){
			System.out.println("Shadow Type Selector ID = "+ n.getId());
			// Getting Shadow type used
			BoaShadowType typeUsed = (BoaShadowType)env.get(callingID.getToken());

			// Im not so sure about this step to replace the tree.
			Factor assign = (Factor)typeUsed.lookupCodegen(n.getId().getToken());
			Identifier x = (Identifier)assign.getOperand();
			// Setting the identifier in teh shadow type eraser tree
			x.setToken(callingID.getToken());
			//Setting the Factor child of teh term node to our tree
			Factor assignTo = test.getLhs();
			assignTo = assign;
			//test =  (Term)n.getParent().getParent();
			//assignTo = test.getLhs();
			//x = (Identifier)assignTo.getOperand();
			//System.out.println(x.getToken());
		}
	}

	
	
	@Override
	public void visit(final Component n) {
		super.visit(n);
		if(n.type instanceof BoaShadowType){
				//Change the Identifier in the ast
				BoaShadowType typeUsed = (BoaShadowType)env.get(n.getType().toString());
				Identifier temp = (Identifier)n.getType();
				System.out.println("Shadow Type Before/After Found = "+ temp.getToken());
				temp.setToken(typeUsed.getDeclarationIdentifierEraser);
				
			
				
				
			}
		
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
				System.out.println("Shadow Type Declaration Found = "+ temp.getToken());
				temp.setToken(typeUsed.getDeclarationIdentifierEraser);
				
				
				//Change the Type in the SymbolTable
				env.setType(n.getId().getToken(),typeUsed.getDeclarationSymbolTableEraser);
				n.type = typeUsed.getDeclarationSymbolTableEraser;
			}
		}
		
	}
}
