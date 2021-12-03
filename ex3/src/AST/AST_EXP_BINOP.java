package AST;

import TYPES.TYPE;
import TYPES.TYPE_INT;
import TYPES.TYPE_STRING;

public class AST_EXP_BINOP extends AST_EXP
{
	int OP;
	public AST_EXP left;
	public AST_EXP right;

	
	/* Operators */

	enum Operator{
		PLUS,
		MINUS,
		TIMES, 
		DIVIDE,
		LT,
		GT,
		EQ
	}


	/* Class Constructor */
	public AST_EXP_BINOP(AST_EXP left, AST_EXP right, int OP)
	{
		SerialNumber = AST_Node_Serial_Number.getFresh(); // SET A UNIQUE SERIAL NUMBER

		// PRINT THE CORRESPONDING DERIVATION RULE
		System.out.print("====================== exp -> exp BINOP exp\n");

		// COPY INPUT DATA NENBERS ... 
		this.left = left;
		this.right = right;
		this.OP = OP;
	}
	
	
	/* The printing message for a binop exp AST node */
	public void PrintMe()
	{
		String sOP="";

		// CONVERT OP to a printable sOP
		if (OP == 0) {sOP = "+";}
		if (OP == 1) {sOP = "-";}
		if (OP == 2) {sOP = "*";}
		if (OP == 3) {sOP = "/";}
		if (OP == 4) {sOP = "<";}
		if (OP == 5) {sOP = ">";}
		if (OP == 6) {sOP = "=";}
		

		// AST NODE TYPE = AST BINOP EXP
		System.out.print("AST NODE BINOP EXP\n");

		
		// RECURSIVELY PRINT left + right
		if (left != null) left.PrintMe();
		if (right != null) right.PrintMe();
		

		// PRINT Node to AST GRAPHVIZ DOT file
		AST_GRAPHVIZ.getInstance().logNode(
			SerialNumber,
			String.format("BINOP(%s)",sOP));
		
	
		// PRINT Edges to AST GRAPHVIZ DOT file
		if (left  != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,left.SerialNumber);
		if (right != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,right.SerialNumber);
	}

	public TYPE SemantMe(){
		TYPE left_type = this.left.SemantMe();
		TYPE right_type = this.right.SemantMe();

		if (OP == 0)
		{
			// OP == "+"
			if (left_type != TYPE_INT.getInstance() || right_type != TYPE_INT.getInstance())
			{
				if (left_type != TYPE_STRING.getInstance() || right_type != TYPE_STRING.getInstance())
				{
					// THE BINARY OPERATION "+" CANNOT BE PERFORMED ON THE TWO SIDES : THROW EXCEPTION : TODO
				}
			}

			// ELSE :

			return left_type;
		}

		// ELSE :

		if (OP == 6)
		{
			// OP == "="

			if (! (left_type.semantically_equals(right_type) || right_type.semantically_equals(left_type)))
			{
				// TYPES CANNOT BE TESTED FOR AN EQUALITY : THROW EXCEPTION : TODO
			}

			return TYPE_INT.getInstance();
		}

		// ELSE : 1 <= OP <= 5 (i.e. OP is one of ["-", "*", "/", "<", ">"])

		if (left_type != TYPE_INT.getInstance() || right_type != TYPE_INT.getInstance())
		{
			// ONE OF THE BINARY OPERATION SIDES TYPE IS NOT AN INTEGER : THROW EXCEPTION : TODO
		}

		// ELSE : EXPERSSION IS VALID AND ITS TYPE IS "TYPE_INT"

		return TYPE_INT.getInstance();
	}
}
