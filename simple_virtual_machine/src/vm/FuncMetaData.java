package vm;

public class FuncMetaData {
	public String name;  // 方法的名称
	public int nargs;  // 方法需要从上一个方法传入多少个参数(从stack pop多少个出来)
	public int nlocals;
	public int address; // 指令的起始地址

	public FuncMetaData(String name, int nargs, int nlocals, int address) {
		this.name = name;
		this.nargs = nargs;
		this.nlocals = nlocals;
		this.address = address;
	}
}
