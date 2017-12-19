
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class DSerialPortListener implements SerialPortEventListener {
	private String appName = "串口通讯";
	private int timeout = 2000;//open 端口时的等待时间

	private CommPortIdentifier commPort;
	private SerialPort serialPort;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	private String portName;	//端口名称
	private int baudRate;		//波特率
	private int dataBits=8;		//数据位
	private int stopBit=1;		//停止位
	private int verifyBit=0;	//检验位
	private String serialHandler; //
	public DSerialPortListener(){}
	public DSerialPortListener(String appName,String portName, int baudRate, int dataBits,
			int stopBit, int verifyBit,String serialHandler) {
		this.appName=appName;
		this.portName = portName;
//		this.portName = portName.toUpperCase();
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBit = stopBit;
		this.verifyBit = verifyBit;
		this.serialHandler=serialHandler;
	}
	
	//返回串口的实例
	public SerialPort getSerialPort() {
		return serialPort;
	}
	
	/**
	 * @方法名称 :listPort
	 * @功能描述 :列出所有可用的串口
	 * @返回值类型 :void
	 */
//	@SuppressWarnings("rawtypes")
	@SuppressWarnings("restriction")
	public static void listPort(){
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		System.out.println("now to list all Port of this PC：" +en);
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL){
				System.out.println(cpid.getName() + ", " + cpid.getCurrentOwner());
			}
		}
	}
	
	
	/**
	 * @方法名称 :selectPort
	 * @功能描述 :选择一个端口，比如：COM1
	 * @返回值类型 :void
	 *	@param portName
	 */
	@SuppressWarnings("rawtypes")
	public void selectPort(){
		
		this.commPort = null;
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& cpid.getName().equals(portName)){
				this.commPort = cpid;
				break;
			}
		}
		
		openPort(portName);
	}
	
	/**
	 * @方法名称 :openPort
	 * @功能描述 :打开SerialPort
	 * @返回值类型 :void
	 */
	private void openPort(String portName){
		if(commPort == null)
			log(String.format("无法找到名字为'%1$s'的串口！",portName));
		else{
			log("端口选择成功，当前端口："+commPort.getName()+",现在实例化 SerialPort:");
			
			try{
				serialPort = (SerialPort)commPort.open(appName, timeout);
				serialPort.setSerialPortParams(baudRate, dataBits, stopBit, verifyBit);
				log("实例 SerialPort 成功！");
			}catch(Exception e){
				throw new RuntimeException(String.format("端口'%1$s'正在使用中！", 
						commPort.getName()));
			}
		}
	}
	
	/**
	 * @方法名称 :checkPort
	 * @功能描述 :检查端口是否正确连接
	 * @返回值类型 :void
	 */
	private void checkPort(){
		if(commPort == null)
			throw new RuntimeException("没有选择端口，请使用 " +
					"selectPort(String portName) 方法选择端口");
		
		if(serialPort == null){
			throw new RuntimeException("SerialPort 对象无效！");
		}
	}
	
	/**
	 * @方法名称 :write
	 * @功能描述 :向端口发送数据，请在调用此方法前 先选择端口，并确定SerialPort正常打开！
	 * @返回值类型 :void
	 *	@param message
	 */
	public void write(byte[] message) {
		checkPort();
		
		try{
			outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		}catch(IOException e){
			throw new RuntimeException("获取端口的OutputStream出错："+e.getMessage());
		}
		
		try{
			outputStream.write(message);
			outputStream.flush();
			log("指令信息"+Arrays.toString(message)+"发送成功！");
		}catch(IOException e){
			throw new RuntimeException("向端口发送信息时出错："+e.getMessage());
		}finally{
			try{
				outputStream.close();
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * @方法名称 :write
	 * @功能描述 :向端口发送数据，请在调用此方法前 先选择端口，并确定SerialPort正常打开！
	 * @返回值类型 :void
	 *	@param message
	 */
	public void writeForSwipeCard(byte[] message) {
		checkPort();
		
		try{
			outputStream = new BufferedOutputStream(serialPort.getOutputStream());
		}catch(IOException e){
			throw new RuntimeException("获取端口的OutputStream出错："+e.getMessage());
		}
		
		try{
			outputStream.write(message);
			outputStream.flush();
		}catch(IOException e){
			throw new RuntimeException("向端口发送信息时出错："+e.getMessage());
		}finally{
			try{
				outputStream.close();
			}catch(Exception e){
			}
		}
	}
	
	/**
	 * @方法名称 :startRead
	 * @功能描述 :开始监听从端口中接收的数据
	 * @返回值类型 :void
	 *	@param time  监听程序的存活时间，单位为秒，0 则是一直监听
	 */
	public void startRead(){
		//listPort();
		selectPort();
		checkPort();
		
		try{
			inputStream = new BufferedInputStream(serialPort.getInputStream());
		}catch(IOException e){
			throw new RuntimeException("获取端口的InputStream出错："+e.getMessage());
		}
		
		try{
			serialPort.addEventListener(this);
		}catch(TooManyListenersException e){
			throw new RuntimeException(e.getMessage());
		}
		
		serialPort.notifyOnDataAvailable(true);
		
		log(String.format("开始监听来自'%1$s'的数据--------------", commPort.getName()));
	}
	
	
	/**
	 * @方法名称 :close
	 * @功能描述 :关闭 SerialPort
	 * @返回值类型 :void
	 */
	public void close(){
		serialPort.close();
		serialPort = null;
		commPort = null;
	}
	
	
	public void log(String msg){
		System.out.println(appName+" --> "+msg);
	}


	/**
	 * 数据接收的监听处理函数
	 */
	public void serialEvent(SerialPortEvent arg0) {
		switch(arg0.getEventType()){
		case SerialPortEvent.BI:/*Break interrupt,通讯中断*/ 
        case SerialPortEvent.OE:/*Overrun error，溢位错误*/ 
        case SerialPortEvent.FE:/*Framing error，传帧错误*/
        case SerialPortEvent.PE:/*Parity error，校验错误*/
        case SerialPortEvent.CD:/*Carrier detect，载波检测*/
        case SerialPortEvent.CTS:/*Clear to send，清除发送*/ 
        case SerialPortEvent.DSR:/*Data set ready，数据设备就绪*/ 
        case SerialPortEvent.RI:/*Ring indicator，响铃指示*/
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:/*Output buffer is empty，输出缓冲区清空*/ 
            break;
        case SerialPortEvent.DATA_AVAILABLE:/*Data available at the serial port，端口有可用数据。读到缓冲数组，输出到终端*/
        	byte[] readBuffer = new byte[1024];
        	byte[] dataBuffer = new byte[4096];
        	int totalLength=0;
            
            try {
            	while (inputStream.available() > 0) {
            		//发送一条数据，接收分好几次接收解决方法
            		Thread.sleep(200);//硬件发送是分段的，加一个延时就行了
                    int length = inputStream.read(readBuffer);
                    System.arraycopy(readBuffer, 0, dataBuffer, totalLength, length);
                    totalLength=totalLength+length;
                }
            	/*
            	 * 原来的代码
	            log("接收到端口返回数据(长度为"+dataBuffer.length+")："+Arrays.toString(dataBuffer));
	            
	            //处理接入数据
            	serialHandler.processMsg(dataBuffer);*/
            	
            	/**
            	 * 处理接入数据
            	 * add by Win
            	 * 因为>>>血压数据传输过程中, 一条记录会分多次传输，导致一次的测量记录不完整
            	 * 所以>>>更改传入的字节数组, 由原来固定4096长度的字节改为具有有效字节数的数组
            	 * 另外>>>这种事件监听的方法是否产生数据丢失, 待测
            	 */
            	//System.out.println(">>>>>>本次接收数据长度："+totalLength);
            	byte[] passBytes = new byte[totalLength]; 
            	System.arraycopy(dataBuffer, 0, passBytes, 0, totalLength);
            	//log("接收到端口返回数据(长度为"+passBytes.length+")："+ Arrays.toString(passBytes));
            	/*
            	 * 将串口实例传入接收方法函数, 用于串口数据交互 
            	 */
            	//serialHandler.processMsg(passBytes);
            	//serialHandler.processMsg(passBytes, serialPort);
            } catch (IOException e) {
            } catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	//如果是USB转串口, 则为 '/dev/ttyUSB0'
	public static void main(String[] args) {
		listPort();
		DSerialPortListener bloodGlucoseHandlerListener=new DSerialPortListener("仪器名称2", "/dev/ttyS0", 9600, 8 , 1, 0, null);
    	bloodGlucoseHandlerListener.startRead();
	}
	
	/**
	 * @方法名称 :查看是否存在指定端口
	 * @功能描述 :选择一个端口，比如：COM1
	 * @返回值类型 :void
	 *	@param portName
	 */
	@SuppressWarnings("rawtypes")
	public boolean checkPortAvailable(){
		boolean flag = false;
		this.commPort = null;
		CommPortIdentifier cpid;
		Enumeration en = CommPortIdentifier.getPortIdentifiers();
		
		while(en.hasMoreElements()){
			cpid = (CommPortIdentifier)en.nextElement();
			if(cpid.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& cpid.getName().equals(portName)){
				this.commPort = cpid;
				break;
			}
		}
		
		if(commPort == null) {
			log(String.format("无法找到名字为'%1$s'的串口！",portName));
			flag = false;
		}
		else{
			log("端口选择成功，当前端口："+commPort.getName()+",串口可用!");
			flag = true;
		}
		return flag;
	}
	
}

