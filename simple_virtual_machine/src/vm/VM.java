package vm;

import java.util.ArrayList;
import java.util.List;

import static vm.Bytecode.BR;
import static vm.Bytecode.BRF;
import static vm.Bytecode.BRT;
import static vm.Bytecode.CALL;
import static vm.Bytecode.GLOAD;
import static vm.Bytecode.GSTORE;
import static vm.Bytecode.HALT;
import static vm.Bytecode.IADD;
import static vm.Bytecode.ICONST;
import static vm.Bytecode.IEQ;
import static vm.Bytecode.ILT;
import static vm.Bytecode.IMUL;
import static vm.Bytecode.ISUB;
import static vm.Bytecode.LOAD;
import static vm.Bytecode.POP;
import static vm.Bytecode.PRINT;
import static vm.Bytecode.RET;
import static vm.Bytecode.STORE;

/** 一个简单的VM 虚拟机 */
public class VM {
	public static final int DEFAULT_STACK_SIZE = 1000;
	public static final int FALSE = 0;
	public static final int TRUE = 1;

	/**
	 * 下面的所有内容放在cpu上的话可以想象成为不同用途的寄存器
	 */
	int ip;             // 指令位置(相当于java的指令计数器PC)
	int sp = -1;  		// 栈指针的位置(单个线程的栈)

	// memory
	int[] code;         // 方法区存放需要执行的指令.  (java的方法区)
	int[] globals;      // 全局变量空间 (java的堆区，多线程控的并发就是控制这里，volatile保证不重拍和可见性，没有保证顺序,慎用)
	int[] stack;		// 栈区 (java的栈)
	Context ctx;		// the active context

	/**
	 * 存储的是每个方法的元数据，比如方法起始所在的指令code区的位置
	 * 比如方法的参数有多少个等等
	 */
	FuncMetaData[] metadata;

	public boolean trace = false;

	public VM(int[] code, int nglobals, FuncMetaData[] metadata) {
		this.code = code;
		globals = new int[nglobals];  // 堆区的初始化大小, java堆区溢出就会OOM
		stack = new int[DEFAULT_STACK_SIZE];  // 默认栈的大小 , 这个溢出就是stackOverflow....那个exception
		this.metadata = metadata;
	}

	/**
	 * 执行入口
	 * @param startip 起始指令的地址
	 */
	public void exec(int startip) {
		ip = startip;
		ctx = new Context(null,0,metadata[0]); // 即模拟调用main函数
		cpu();
	}

	/** 模拟执行单个方法的循环，这里是核心步骤了 */
	protected void cpu() {
		int opcode = code[ip];
		int a,b,addr,regnum;
		while (opcode!= HALT && ip < code.length) {
			if ( trace ) System.err.printf("%-35s", disInstr());
			ip++; //跳转到下一个指令去
			switch (opcode) {
				case IADD:
					b = stack[sp--];   			// 栈顶数字，出栈
					a = stack[sp--]; 			// 栈顶数字，出栈
					stack[++sp] = a + b;      	// 结果入栈
					break;
				case ISUB:
					b = stack[sp--];  // 减法同上
					a = stack[sp--];
					stack[++sp] = a - b;
					break;
				case IMUL: // 乘法同上
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a * b;
					break;
				case ILT :  // 同上，结果入栈
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a < b) ? TRUE : FALSE;
					break;
				case IEQ :  // 同上，结果入栈
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a == b) ? TRUE : FALSE;
					break;
				case BR : // 跳过下一个指令
					ip = code[ip++];
					break;
				case BRT : // 跳转
					addr = code[ip++];
					if ( stack[sp--]==TRUE ) ip = addr;
					break;
				case BRF : // 跳转
					addr = code[ip++];
					if ( stack[sp--]==FALSE ) ip = addr;
					break;
				case ICONST: // 常量
					stack[++sp] = code[ip++]; // 入栈即可
					break;
				case LOAD : // 获取下一个指令的值
					regnum = code[ip++];
					stack[++sp] = ctx.locals[regnum];  // 本地变量中加载值入栈
					break;
				case GLOAD :
					addr = code[ip++];
					stack[++sp] = globals[addr];  // 全局变量中加载值入栈
					break;
				case STORE :
					regnum = code[ip++];
					ctx.locals[regnum] = stack[sp--];  // 本地变量中存储栈顶的值
					break;
				case GSTORE :
					addr = code[ip++];
					globals[addr] = stack[sp--];  // 全局变量中存储栈顶的值
					break;
				case PRINT :
					System.out.println(stack[sp--]);  // 打印栈顶的值
					break;
				case POP:
					--sp;
					break;
				case CALL :
					int findex = code[ip++];			// 寻找调用的方法index
					int nargs = metadata[findex].nargs;	// 获取方法的参数个数
					ctx = new Context(ctx,ip,metadata[findex]);  // 配置方法的上下文，以及当前指令的地址
					// 把入栈的所有参数推入到本次调用的locals里面
					int firstarg = sp-nargs+1;
					for (int i=0; i<nargs; i++) {
						ctx.locals[i] = stack[firstarg+i];
					}
					sp -= nargs;  // 栈顶指针放下来
					ip = metadata[findex].address;		// 跳转到方法的指向的指令地址
					break;
				case RET:
					ip = ctx.returnip;
					ctx = ctx.invokingContext;			// 通过context链退出, 继续返回调用的ip指令地址执行
					break;
				default :
					throw new Error("非法的执行指令: "+opcode+", ip:"+(ip-1));
			}
			if ( trace ) System.err.printf("%-22s %s\n", stackString(), callStackString());
			opcode = code[ip];
		}
		if ( trace ) System.err.printf("%-35s", disInstr());
		if ( trace ) System.err.println(stackString());
		if ( trace ) dumpDataMemory();
	}

	protected String stackString() {
		StringBuilder buf = new StringBuilder();
		buf.append("stack=[");
		for (int i = 0; i <= sp; i++) {
			int o = stack[i];
			buf.append(" ");
			buf.append(o);
		}
		buf.append(" ]");
		return buf.toString();
	}

	/**
	 * 通过context把调用链打印出来
	 * @return
	 */
	protected String callStackString() {
		List<String> stack = new ArrayList<String>();
		Context c = ctx;
		while ( c!=null ) {
			if ( c.metadata!=null ) {
				stack.add(0, c.metadata.name);
			}
			c = c.invokingContext;
		}
		return "calls="+stack.toString();
	}

	/**
	 * 负责把栈区里面的内容打印出来和调用链打印出来
	 * 就是调试，别看性能了
	 * @return
	 */
	protected String disInstr() {

		int opcode = code[ip];
		String opName = Bytecode.instructions[opcode].name;
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("%04d:\t%-11s", ip, opName));
		int nargs = Bytecode.instructions[opcode].n;
		if ( opcode==CALL ) {
			buf.append(metadata[code[ip+1]].name);
		}
		else if ( nargs>0 ) {
			List<String> operands = new ArrayList<String>();
			for (int i=ip+1; i<=ip+nargs; i++) {
				operands.add(String.valueOf(code[i]));
			}
			for (int i = 0; i<operands.size(); i++) {
				String s = operands.get(i);
				if ( i>0 ) buf.append(", ");
				buf.append(s);
			}
		}
		return buf.toString();
	}

	protected void dumpDataMemory() {
		System.err.println("global内存(堆区)简要情况:");
		int addr = 0;
		for (int o : globals) {
			System.err.printf("%04d: %s\n", addr, o);
			addr++;
		}
		System.err.println();
	}
}
