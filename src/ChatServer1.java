import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class ChatServer1 {
	public static void main(String[] args) {
		try {// 현재 내 네트워크 정보를 가지고 있음
			InetAddress inet = InetAddress.getLocalHost();
			System.out.println("내 실제 아이피 : " + inet.getHostAddress());
			
			String localhost = inet.toString();
			
			// split()이랑 유사함 -> 잘라서 가져올 수 있음 
			StringTokenizer st = new StringTokenizer(localhost, "/");
			while(st.hasMoreTokens()) {
				System.out.println(st.nextToken());
			}
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
