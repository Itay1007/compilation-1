package AST;

import TYPES.*;
import TEMP.*;

public abstract class AST_EXP extends AST_Node
{	
	public abstract BOX SemantMe() throws Exception;

	public abstract TEMP IRMe();
}