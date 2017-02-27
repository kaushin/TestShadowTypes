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
import boa.compiler.ast.Conjunction;
import boa.compiler.ast.Call;
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

	private class Replace extends AbstractVisitorNoArg{
		protected Expression result = null;
		protected String CallingVariableName ;
		protected Expression test = null;
		protected void initialize() { 
			
		}

		public Expression getExpression(){
			CallingVariableName = null;
			return result;
		}

		public void start(final Node n, String CallingVariableName) {
			initialize();
			this.CallingVariableName = CallingVariableName;
			n.accept(this);
			
			
		}


		public void start(final Node n, Expression test) {
			initialize();
			this.test = test;
			n.accept(this);
		}

		// public void visit(final Call n) {
		// 	if(test != null){
		// 		ArrayList<Expression> args = new ArrayList<Expression>();
		// 		args.add(test);
		// 		n.setArgs(args);
		// 		System.out.println("Replaced Expression");
		// 	}
		// 	super.visit(n);
		// }


		public void visit(final Factor n) {
			super.visit(n);
			
			if(n.getOperand() instanceof Identifier){
				Identifier id = (Identifier)n.getOperand();
				System.out.println("---->"+id.getToken() + " string: " + CallingVariableName);
				if(id.getToken().equals("${0}")){
					id.setToken(CallingVariableName);
					result = (Expression)n.getParent().getParent().getParent().getParent().getParent();
				}
			}
		}
	}


	@Override
	public void visit(final Selector n) {
		super.visit(n);

		env = n.env;
		//Get Term node and replace its child(I was thinking this is where I need to replace the tree)
		Factor test =  (Factor)n.getParent();
		

		if ( test.getOperand().type instanceof BoaShadowType){
			//get parent Expression or Call
			Node temp = n;
			Call c = null ;
			//Is it possible for the call statement to have more than one expression in this case?
			while(c == null ){
				if(temp instanceof Call){
					c = (Call)temp;
					System.out.println(c.getArgsSize());
				}else{
					temp = temp.getParent();
				}
			}
			
			//c = c.addArg( new Expression());
			Identifier id = (Identifier)test.getOperand();
			// Getting Shadow type used
			System.out.println(id.getToken());
			BoaShadowType typeUsed = (BoaShadowType)env.get(id.getToken());	

			Expression replacement = (Expression)typeUsed.lookupCodegen(n.getId().getToken()).clone();
			//working through to all identifiers to all identitiers!!
			Replace rep = new Replace();

			rep.start(replacement,id.getToken());
			replacement = rep.getExpression();//now replacement has all identifiers replaced
			ArrayList<Expression> args = new ArrayList<Expression>();
			//calling to replace children of call node
			args.add(replacement);
			c.setArgs(args);
			
			

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
