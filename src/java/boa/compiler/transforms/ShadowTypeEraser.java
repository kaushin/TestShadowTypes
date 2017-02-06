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

import boa.compiler.ast.Factor;
import boa.compiler.ast.Selector;
import boa.compiler.ast.Term;
import boa.compiler.ast.Node;
import boa.compiler.ast.Component;
import boa.compiler.ast.expressions.*;

import boa.types.BoaShadowType;

/**
 * Converts a tree using shadow types into a tree without shadow types.
 *
 * @author rdyer
 * @author kaushin
 */
public class ShadowTypeEraser extends AbstractVisitorNoArg {

	private ArrayList<String> shadowTypes ;

	public ShadowTypeEraser(){
		shadowTypes =  new ArrayList<String>(Arrays.asList("IfStatement","ForStatement"));
	}


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
		if(shadowTypes.contains(n.getType().toString())){
			System.out.println(  "Shadow Type Found : " + n.getType() );
		}


		n.getType().accept(this);
	}
}
