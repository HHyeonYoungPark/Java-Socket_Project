import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatServerGUI1 extends JFrame implements ActionListener{
	JTextField tfIp,tfPort,tfMsg;
	JButton btnStart,btnSend;
	JLabel lblIp,lblPort;
	JTextArea ta;
	HashMap<String, PrintWriter> hm;
	JList<String> list;
	DefaultListModel<String> model;
	
	public ChatServerGUI1() {
		try {
			InetAddress inet = InetAddress.getLocalHost();
			String title = inet.getHostAddress();
			this.setTitle(title);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		model = new DefaultListModel<>();
		list = new JList<>(model);
		model.addElement("==접속자목록==");
		
		hm = new HashMap<>();
		// 여기에 모든 클라이언트의 정보(아이디,이클라이언트에 글을 보낼수있는(pw)통로)
		
		this.setSize(500, 700);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		lblIp = new JLabel("IP : ");
		lblPort = new JLabel("PORT : ");
		tfIp = new JTextField("127.0.0.1",10);//localhost 랑 같은말
		tfPort = new JTextField("10001",5);
		btnStart = new JButton("서버시작");
		JPanel pnl1 = new JPanel();
		pnl1.add(lblIp);
		pnl1.add(tfIp);
		pnl1.add(lblPort);
		pnl1.add(tfPort);
		pnl1.add(btnStart);
		
		ta = new JTextArea();				
		JScrollPane sp = new JScrollPane(ta);
		sp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {	
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				JScrollBar src = (JScrollBar)e.getSource();
				src.setValue(src.getMaximum());			
			}
		});
		
		tfMsg = new JTextField(30);
		btnSend = new JButton("공지전송");
		JPanel pnl2 = new JPanel();
		pnl2.add(tfMsg);
		pnl2.add(btnSend);
		
		btnStart.addActionListener(this);
		btnSend.addActionListener(this);
		
		this.add(pnl1,"North");
		this.add(sp);
		this.add(pnl2,"South");
		
		this.add(list,"East");
		
		this.setVisible(true);		
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==btnStart) {// 메서드-드리븐 , 이벤트-드리븐 패턴
			startServer();			 // 버튼 하나마다 메서드 한개씩 매칭
		}else if(e.getSource()==btnSend) {
			sendMsg();
		}
	}
	
	
	public void startServer() {		   
		try {
			
			int port = Integer.parseInt(tfPort.getText());
			ServerSocket server = new ServerSocket(port);
			ta.append("[알림] 서버가 실행되었습니다.\n");			
			ta.append("[알림] 유저의 접속을 기다립니다.\n");// 실제로 accept에서 하는 일인데 accept실행되면 멈추니까 미리 써놓기
			
			Thread t = new Thread(new Runnable() {				
				@Override
				public void run() {
					while(true) {						
						try {
							Socket socket = server.accept();// 접속 대기
							// 메인에서 하는일은 서버실행을 위한 준비작업. 실제는 쓰레드가 다함
							ChatThread3 ct = new ChatThread3(socket, hm, ta, model);// 실제 채팅에 대한 내용 전부다 쓰레드에 던지기
							ct.start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}// 클라이언트 접속전까지 이후작업X
						// accept() 이후의 작업은 클라이언트가 접속해야 실행됨
						// 쓰레드 시작
					}
				}
			});
			t.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	
	public void sendMsg() {//서버에서 직접 보내는 공지
		// map 전체읽어와서 pw를 통해서 전체다 보내기
		
		if(tfMsg.getText().indexOf("/ban ")==0) {
			String banid = tfMsg.getText().split(" ")[1];
			hm.get(banid).println("/quit ");
			hm.get(banid).flush();
			
		}else if(tfMsg.getText().indexOf("/cl ")==0){
			if(tfMsg.getText().split(" ").length==1) {
//				JOptionPane.showMessageDialog(ta, "채팅창이 초기화됩니다");
				ta.setText("");			
			}else {
				String taClearId = tfMsg.getText().split(" ")[1];
				hm.get(taClearId).println("/cl ");
				hm.get(taClearId).flush();
			}
			
		}else if(tfMsg.getText().indexOf("/off ")==0) {
			String btnEnbles = tfMsg.getText().split(" ")[1];
			hm.get(btnEnbles).println("/off ");
			hm.get(btnEnbles).flush();
			
		}else if(tfMsg.getText().indexOf("/on ")==0) {
			String btnEnbles = tfMsg.getText().split(" ")[1];
			hm.get(btnEnbles).println("/on ");
			hm.get(btnEnbles).flush();
			
		}else if(tfMsg.getText().indexOf("/boom")==0) {
			for(Entry<String, PrintWriter> entrySet:hm.entrySet()) {
				entrySet.getValue().println("/boom ");
				entrySet.getValue().flush();
			}
			
		}else {
			for(Entry<String, PrintWriter> entrySet:hm.entrySet()) { 
				entrySet.getValue().println("[알림]"+tfMsg.getText());
				entrySet.getValue().flush();
			}
		}		
	}
	
	
	
	public static void main(String[] args) {
		new ChatServerGUI1();
	}	
}



class ChatThread3 extends Thread{// 쓰레드를 상속, 구현, 메서드로
	// 클라이언트 마다 생성됨. 클라이언크 개수마다 생성됨.
	
	Socket socket;
	HashMap<String, PrintWriter> hm;
	JTextArea ta;
	// 쓰레드에서 반드시 해야할 일   *입력<=>출력
	BufferedReader br;// 클라이언트가 넘겨주는 내용을 읽기위해 필요
	String id;// 클라이언트가 넘겨주면 받아서 넣음
	DefaultListModel<String> model;
	public ChatThread3(Socket socket, HashMap<String, PrintWriter> hm, JTextArea ta, DefaultListModel<String> model) {
		this.socket=socket;// 메인에서 받아온 값을 필드(전역변수)로 옮겨줌
		this.hm = hm;// 메인에서 받아온 값을 필드(전역변수)로 옮겨줌
		this.ta = ta;
		this.model = model;
		try {
			OutputStream os = socket.getOutputStream();
			// 클라이언트로 넘겨줄 데이터가 갈 stream - 가장 기본
			OutputStreamWriter osw = new OutputStreamWriter(os);
			PrintWriter pw = new PrintWriter(osw);
		// println 을 쓸수 있는 보조 스트림 => 우리는 이것을 통해서 클라이언트로 값을 보냄
			
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			// 한줄씩 읽는 readLine() 메서드를 가지는 보조 스트림
			// 이것을 통해서 클라이언트에서 들어오는 값을 읽어올수 있다.
			
			// 클라이언트 접속시 무조건 id를 받으므로 if로 br.readLine()를 물어볼 필요X
			id = br.readLine();			
			ta.append(id+"님께서 접속 하셨습니다.\n");
			
			// 리스트에 접속자 추가하기 
			model.addElement(id);
			
			hm.put(id, pw);//아이디 ,출력스트림
			
			broadcast("[알림]"+id+"님께서 접속 하셨습니다.");// 전체 방송
			
			refreshMember();
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}// 생성자 => Thread run 하기 전까지의 작업
	
	public void refreshMemberServer() {
		// 서버에 새 클라이언트 맴버 갱신
		model.removeAllElements();
		model.addElement("===접속자목록=");
		for(Entry<String, PrintWriter> entrySet:hm.entrySet()) {
			model.addElement(entrySet.getKey());
		}
	}
	
	public void refreshMember() {
		// 클라이언트에 새접속자 목록 알리기
		String members="";
		for(Entry<String, PrintWriter> entrySet:hm.entrySet()) {
			members += entrySet.getKey()+"#";
		}
		// 접속자 목록을 1개의 문자열로 만들기 ex) aaa#bbb#ccc#
		broadcast("/members "+members); // 전체 전송	
		
		//         /members aaa#bbb#ccc#
		// 새접속자 목록 알리기 끝
	}
	
	
	
	@Override
	public void run() { // 클라이언트로 부터 들어오는 내용 계속 받기
		try {			
				String line = null;
				while((line = br.readLine())!=null) {
					ta.append(id+":"+line+"\n");// 서버에 출력
					// 넘어온 글씨의 내용의 첫번째가 "/quit " 로 시작한다면 실행
					if(line.indexOf("/quit ")==0) {
						broadcast("[알림]"+id+"님이 종료하셨습니다");
						hm.remove(id);// 종료가 되므로 hashmap에서도 지워줌						
						refreshMember();
						refreshMemberServer();
						break;
						
					}else if(line.indexOf("/to ")==0){// /to 아이디
						String wid = line.split(" ")[1];
						hm.get(wid).println("["+id+"]님의 귓속말 : "+line);
						
					}else {					
						// 클라이언트에게 다시 출력하기
						broadcast(id+":"+line);
					}
				}				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void broadcast(String msg) {
		//접속한 모든 pw를 이용하여 전송 => hashmap이 다 들고 있음. hm
		
		/*Set<String> keys = hm.keySet();
		Iterator<String> keyIter = keys.iterator();
		while(keyIter.hasNext()) {
			String key = keyIter.next();
			PrintWriter bpw = hm.get(key);
			bpw.println(msg);
			bpw.flush();
		}*/
		
		for(Entry<String, PrintWriter> entrySet:hm.entrySet()) {
			entrySet.getValue().println(msg);
			entrySet.getValue().flush();
		}
	}
}
