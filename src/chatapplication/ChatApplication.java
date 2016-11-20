/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Neloy
 */
public class ChatApplication {

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        ChatUser cu = new ChatUser();
    }
    
}

class SendAction extends Thread{
	UserInternal u;
	public void run(){
		Scanner sobj = new Scanner(System.in);
			String msg;
			while(true){
				try{
					System.out.print("You: ");
					msg = sobj.next();
					u.sendMessage(msg);
				}
				catch(Exception e){}
			}
	}
}

class ReceiveAction extends Thread{
	UserInternal u;
        String msg;
	public void run(){
			while(true){
				try{
                                    msg = u.receiveMessage();
                                    u.cb.addMessageToChatBox(msg, false);
				}
				catch(Exception e){}
			}
	}
}

class UserInternal extends Thread{
	int senderPort;
	int receiverPort;
	DatagramSocket mySocketS;
	DatagramSocket mySocketR;
	InetAddress receiver;
        ChatBox cb;

    UserInternal() {
    }
	
	public void sendMessage(String msg) throws Exception{
		byte[] buffer = msg.getBytes();
		DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, receiver, senderPort);
		mySocketS.send(datagram);
	}

	public String receiveMessage() throws Exception{
			byte[] msg = new byte[1000];
			DatagramPacket dpacket = new DatagramPacket(msg, 1000);
			mySocketR.receive(dpacket);
			String rmsg = new String(msg);
			return rmsg.trim();
	}
        
        void setMemberValues(InetAddress r, int rport, int sport) throws SocketException{
            receiver = r;
            receiverPort = rport;
            senderPort = sport;
            mySocketS = new DatagramSocket();
            mySocketR = new DatagramSocket(receiverPort);
        }
        
	UserInternal(String[] args) throws Exception{
		if(args.length!=2){
			System.out.println("Enter SenderPortNo and ReceiverPortNo!");
		}
		else{
			receiver = InetAddress.getLocalHost();
			senderPort = Integer.parseInt(args[0]);
			receiverPort = Integer.parseInt(args[1]);
			mySocketS = new DatagramSocket();
			mySocketR = new DatagramSocket(receiverPort);
		}
	}
}


class ChatUser{
        Initiate i;
        UserInternal u;
        ReceiveAction r;
        SendAction s;
	ChatUser() throws Exception{
		u = new UserInternal();
		r = new ReceiveAction();
		r.u = u;
		//s = new SendAction();
		//s.u = u;
		i = new Initiate(u);
                r.start();
                //System.out.println(u.receiver);
	}
}

class Initiate implements ActionListener{
    JFrame f = null;
    JTextField IPOfOtherUser = null;
    JTextField otherUserPort = null;
    JTextField ourPort = null;
    JLabel l1,l2,l3;
    JButton b = null;
    Font font = null;
    UserInternal u = null;

    Initiate(UserInternal u) {
        this.u = u;
        f = new JFrame("ChatApplication");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(300,300);
        f.setLocation(600, 250);
        f.setLayout(null);
        
        IPOfOtherUser = new JTextField(30);
        otherUserPort = new JTextField(10);
        ourPort = new JTextField(10);
        l1 = new JLabel("IP of other User");
        l2 = new JLabel("Other user's Port");
        l3 = new JLabel("Your Port");
        font = new Font("Segoe UI",Font.PLAIN,14);
        l1.setFont(font);
        l2.setFont(font);
        l3.setFont(font);
        Font fnt = new Font("Consolas",Font.BOLD,12);
        IPOfOtherUser.setFont(fnt);
        otherUserPort.setFont(fnt);
        ourPort.setFont(fnt);
        
        l1.setBounds(10, 20, 100, 30);
        IPOfOtherUser.setBounds(140, 20, 130, 30);
        l2.setBounds(10, 70, 130, 30);
        otherUserPort.setBounds(140, 70, 100, 30);
        l3.setBounds(10, 120, 100, 30);
        ourPort.setBounds(140, 120, 100, 30);
        
        b = new JButton("Proceed!");
        b.setBounds(30,170,100,30);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setForeground(Color.BLUE);
        b.setBackground(Color.CYAN);
        b.addActionListener(this);
        
        f.add(l1);
        f.add(IPOfOtherUser);
        f.add(l2);
        f.add(l3);
        f.add(otherUserPort);
        f.add(ourPort);
        f.add(b);
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            u.setMemberValues(InetAddress.getByName(IPOfOtherUser.getText()), Integer.parseInt(otherUserPort.getText()), Integer.parseInt(ourPort.getText()));
            u.cb = new ChatBox(u);
            f.dispose();
        } catch (SocketException ex) {
            Logger.getLogger(Initiate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Initiate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

class ChatBox implements ActionListener{
    JFrame f = null;
    JTextArea textArea = null;
    JScrollPane ofTextArea = null;
    JScrollPane ofTextPane = null;
    JButton sendButton = null;
    JTextPane textPane = null;
    Font font = null;
    UserInternal u = null;
    ChatBox(UserInternal u){
        this.u = u;
        font = new Font("Consolas", Font.PLAIN, 12);
        f = new JFrame("ChatApplication");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(300,400);
        f.setLocation(600, 250);
        f.setLayout(null);
        
        textArea = new JTextArea(3, 200);
        textArea.setFont(font);
        ofTextArea = new JScrollPane(textArea);
        ofTextArea.setBounds(5, 326, 200, 30);
        ofTextArea.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        ofTextArea.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        
        sendButton = new JButton("Send");
        sendButton.setBounds(205, 326, 75, 30);
        sendButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        sendButton.setForeground(Color.BLUE);
        sendButton.setBackground(Color.CYAN);
        sendButton.addActionListener(this);

        textPane = new JTextPane();
        textPane.setFont(new Font("Segoe UI",Font.PLAIN,12));
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_CENTER);
        textPane.setCharacterAttributes(attributeSet, true);
        textPane.setEditable(false);
        
        ofTextPane = new JScrollPane(textPane);
        ofTextPane.setBounds(5,5,275,317);
        ofTextPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        ofTextPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        ofTextPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
            e.getAdjustable().setValue(e.getAdjustable().getMaximum());
        }});
        
        f.add(ofTextPane);
        f.add(ofTextArea);
        f.add(sendButton);
        f.setVisible(true);
    }
    
    synchronized void addMessageToChatBox(String message, boolean isUs){
        String m;
        message = message.trim();
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        if(isUs){
            StyleConstants.setForeground(keyWord, Color.RED);
            StyleConstants.setBackground(keyWord, Color.YELLOW);
            //StyleConstants.setBold(keyWord, true);
            StyleConstants.setFontSize(keyWord, 16);
            m = "\n\nYou: " + message;
        }
        else{
            StyleConstants.setForeground(keyWord, Color.YELLOW);
            StyleConstants.setBackground(keyWord, Color.RED);
            StyleConstants.setFontSize(keyWord, 16);
            //StyleConstants.setBold(keyWord, true);
            m = "\n\nOther: " + message;
        }
        try{
            doc.insertString(doc.getLength(), m, keyWord );
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Oops! Error while displaying message!");
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = textArea.getText();
        if(msg.length() > 0){
            try {
                u.sendMessage(msg);
            } catch (Exception ex) {
                Logger.getLogger(ChatBox.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Oops! Error while sending message!");
                
            }
            addMessageToChatBox(msg, true);
            textArea.setText("");
        }
    }
}