package AST;

import IR.*;
import SYMBOL_TABLE.*;
import TYPES.*;
import TEMP.*;

public class AST_VARDEC_NEWEXP extends AST_VARDEC {
    // DATA MEMBERS
	public AST_TYPE type;
	public String name; 
	public AST_NEWEXP newExp;
	public int line;

	// metadata for code generation :
	public TYPE_CLASS _cls_ = null;

	// Class Constructor
	public AST_VARDEC_NEWEXP(AST_TYPE type, String name, AST_NEWEXP newExp, int line)
	{
		// SET A UNIQUE SERIAL NUMBER
		SerialNumber = AST_Node_Serial_Number.getFresh();

		// PRINT CORRESPONDING DERIVATION RULE
		System.out.format("====================== varDec -> type ID(%s) ASSIGN newExp SEMICOLON\n", name);

		// COPY INPUT DATA NENBERS
		this.type = type;
		this.name = name;
		this.newExp = newExp;

		this.line = line;
	}


	// The printing message for a statement list AST node
	public void PrintMe()
	{
		// AST NODE TYPE = AST STATEMENT LIST
		System.out.print("AST NODE NEWEXP VARDEC\n");

		// RECURSIVELY PRINT HEAD + TAIL
		if (type != null) type.PrintMe();
		if (newExp != null) newExp.PrintMe();

		// PRINT to AST GRAPHVIZ DOT file
		AST_GRAPHVIZ.getInstance().logNode(
			SerialNumber,
			String.format("NEWEXP ASSIGN\ntype ID(%s) := right\n", name));
		
		// PRINT Edges to AST GRAPHVIZ DOT file
		if (type != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,type.SerialNumber);
		if (newExp != null) AST_GRAPHVIZ.getInstance().logEdge(SerialNumber,newExp.SerialNumber);
	}


	public BOX SemantMe() throws Exception
	{
		return SemantMe(null);	
	}

	public BOX SemantMe(TYPE_CLASS cls) throws Exception
	{
		if (cls != null)
		{
			if (SYMBOL_TABLE.getInstance().find_at_class(cls, this.name) != null)
			{
				// A VARIABLE WITH THE SAME NAME WAS ALLREADY DECLARED AT THE GIVEN CLASS : THROW ERROR :
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}
		}

		if (SYMBOL_TABLE.getInstance().find_at_curr_scope(this.name) != null)
		{
			// A VARIABLE WITH THAT NAME WAS ALREADY DECLARED AT OUR SCOPE : THROW ERROR :
			String cls_name = this.getClass().getName();
			throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
		}

		// ELSE : 

		TYPE var_type = this.type.SemantMe().type;
		BOX exp_box = this.newExp.SemantMe();
		TYPE exp_type = exp_box.type;

		if (cls != null)
		{
			if (! exp_box.is_const)
			{
				// CANNOT ASSIGN A CLASS FIELD A NON-CONSTANT VALUE
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}
		}

		if (exp_box.is_array)
		{
			if ( ! var_type.is_array())
			{
				// var is not an array and newExp is: THROW EXCEPTION :
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}

			// else:

			if ( ! ((TYPE_ARRAY) var_type).elems_type.semantically_equals(exp_type))
			{
				// newExp array type is not compatible with var type : THROW EXCEPTION :
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}
		}
		else
		{
			if (var_type.is_void())
			{
				// VARIABLE CANNOT BE OF TYPE VOID : THROW EXCEPTION
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}

			if (var_type.semantically_equals(exp_type) == false)
			{
				// the newExp cannot be assigned to the variable : THROW EXCEPTION :
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}
		}

		if (cls != null)
		{
			// CHECK THAT THE VARIABLE DOES NOT SHADOW AN ANOTHER CLASS FIELD :

			if (cls.get_field(this.name) != null)
			{
				String cls_name = this.getClass().getName();
				throw new Exception("SEMANTIC ERROR : " + this.line + " : " + cls_name);
			}
		}

		boolean isLocalVar;
		boolean isClassField;

		if (cls != null)
		{
			// it is a class field

			isLocalVar = false;
			isClassField = true;
			this._cls_ = cls;
		}
		else{
			if (SYMBOL_TABLE.getInstance().find_curr_scope_function() != null)
			{
				// it is a local variable

				isLocalVar = true;
				isClassField = false;
			}
			else
			{
				// it is a global variable

				isLocalVar = false;
				isClassField = false;
			}
		}

		SYMBOL_TABLE.getInstance().enter(this.name,
										var_type,
									false,
										isLocalVar,
										isClassField);
		

		if (cls != null)
		{
			// MAKE A CLASS FIELD OBJECT OUT OF THIS DECLARATION (WRAP IT WITH A NAME)
			if (exp_box != null)
			{
				// class field has a CONSTANT initial value
				var_type = new TYPE_CLASS_FIELD(var_type, this.name, exp_box.initial_value, cls);
			}
			else
			{
				// class field doesn't have an initial value
				var_type = new TYPE_CLASS_FIELD(var_type, this.name, cls);
			}
		}

		SYMBOL_TABLE_ENTRY entry = SYMBOL_TABLE.getInstance().find_entry(this.name);
		this.setCodeGenMetaData(entry);

		return new BOX(var_type);
	}

	public void setCodeGenMetaData(SYMBOL_TABLE_ENTRY entry)
	{
		this.offset = entry.offset;
		this.isArg = entry.isArg;
		this.isLocalVar = entry.isLocalVar;
		this.isClassField = entry.isClassField;
	}

	public void IRme()
	{
		// WE ARE ASSUMING THAT IF WE ARE HERE THE DECLARATION IS NOT OF A CLASS FIELD

		if (newExp != null)
		{
			TEMP initial_value = newExp.IRme();

			IR.
			getInstance().
			Add_IRcommand(new IRcommand_Store(name,
											  this._cls_, // if its null then isClassField is False and no error occures
											  initial_value, 
											  this.isLocalVar,
											  this.isArg,
											  this.isClassField,
											  this.offset));
		}
	}
}
