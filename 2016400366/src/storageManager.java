package dbSystem;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;


public class storageManager {
	private static List<DatabaseInstruction> instructionList = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		Scanner input = new Scanner(new File(args[0]));
		File out = new File(args[1]);
		PrintWriter output = new PrintWriter(out);
		
		File data = new File("dataDic.txt");
		RandomAccessFile dataDic = new RandomAccessFile("dataDic.txt", "rw");


		if(!input.hasNextLine()) return;

		
		
		while(input.hasNextLine()) {
			instructionList.add(getInstruction(input.nextLine()));
		}
		

		runInstruction(dataDic,output);
		output.close();
		dataDic.close();

	}



	private static DatabaseInstruction getInstruction(String line) {
		DatabaseInstruction instruction = new DatabaseInstruction();
		String[] args = line.split(" ");

		instruction.setCommand(args[0]);
		instruction.setType(args[1]);
		
		if(args.length ==3) {
			instruction.setName(args[2]);
		}

		if (args.length > 3) {
			instruction.setName(args[2]);
			instruction.addArgument(args[3]);

			for (int i = 4; i < args.length; i++) {
				instruction.addArgument(args[i]);
			}

		}

		return instruction;
	}

	public static void runInstruction(RandomAccessFile dataDic,PrintWriter output) throws IOException {
		
		
		for (int i = 0; i < instructionList.size(); i++) {
			DatabaseInstruction databaseInstruction = instructionList.get(i);

			if (databaseInstruction.getCommand().equals("create")) {
				if (databaseInstruction.getType().equals("type")) {
					String f = databaseInstruction.getName() + ".txt";
					File file = new File(f);
					if(!file.exists()){
						file.createNewFile();

						RandomAccessFile filendType = new RandomAccessFile(file,"rw");
						filendType.writeByte(1); 
						filendType.writeByte(Integer.parseInt(databaseInstruction.getArgument(0))); 
						filendType.writeByte(0); 
						filendType.close();
					}
					dataDic.seek(dataDic.length());
					dataDic.writeByte(0);

					int rest = 10 - Integer.parseInt(databaseInstruction.getArgument(0));
					String s = databaseInstruction.getName();
					while(s.length()<11)
						s += "#";
					dataDic.writeUTF(s);
					dataDic.writeInt(Integer.parseInt(databaseInstruction.getArgument(0)));
					for(int j=1;j<databaseInstruction.getArgumentCount();j++){
						String k = databaseInstruction.getArgument(j);
						while(k.length()<11)
							k += "#";
						dataDic.writeUTF(k);

					}
					for(int j=0;j<rest;j++){
						dataDic.writeUTF("###########");
					}


				} else {
					String fileName= databaseInstruction.getName() + ".txt";
					File f = new File(fileName);
					if(f.exists()){
						RandomAccessFile filendType = new RandomAccessFile(fileName, "rw");
						filendType.seek(0);
						int numberPage = filendType.readByte();
						filendType.seek(1); 
						int numberField = filendType.readByte();
						int recordHold = numberField*4+1; 
						int max = (recordHold*40+1)*10+2;
						for(int k=1;k<filendType.length() && k<max ;k+=40*recordHold+1){
							filendType.seek(k+1); 

							int numberRecords = filendType.readByte();

							numberRecords++;
							if(numberRecords<=40){
								filendType.seek(k+1);
								filendType.writeByte(numberRecords);
								filendType.seek(k+2+(numberRecords-1)*recordHold);
								filendType.writeByte(0); 
								for(int j=0;j<numberField;j++){
									filendType.writeInt(Integer.parseInt(databaseInstruction.getArgument(j)));
								}
								if(numberRecords==40){
									filendType.seek(filendType.length());
									filendType.writeByte(0);
								}
							}
						}
						filendType.close();
					}
				}
			}

			if (databaseInstruction.getCommand().equals("list")) {
				if (databaseInstruction.getType().equals("type")) {
					PriorityQueue<String> types = new PriorityQueue<String>();

					for(int j=0; j <dataDic.length();j+=148){
						dataDic.seek(j);

						if(dataDic.readByte()==0){
							dataDic.seek(j+1);
							String s = dataDic.readUTF();
							s = s.substring(0,s.indexOf('#'));
							types.add(s);
						}
					}
					int size = types.size();
					for(int j=0;j<size;j++){
						String s = types.poll();
						output.println(s);
					}
				} else {

					LinkedList<LinkedList<Integer>> records = new LinkedList<LinkedList<Integer>>();
					String fileName= databaseInstruction.getName() + ".txt";
					File f = new File(fileName);
					if(f.exists()){
						RandomAccessFile filendType = new RandomAccessFile(fileName, "rw");
						filendType.seek(0);
						int numberPage = filendType.readByte();
						filendType.seek(1); 
						int numberField = filendType.readByte();
						int recordHold = numberField*4+1; 
						int max = (recordHold*40+1)*10+2;
						for(int k=1;k<filendType.length() && k<max ;k+=40*recordHold+1){
							filendType.seek(k+1); 
							int numberRecords = filendType.readByte();
							for(int l=0;l<=numberRecords*recordHold;l+=recordHold){
								if(filendType.length()>l+k+3){
									filendType.seek(l+k+2);
									int emptyBit = filendType.readByte(); 
									if(emptyBit==0){
										LinkedList<Integer> record = new LinkedList<Integer>();
										for(int j=0;j<numberField;j++){
											filendType.seek(l+k+3+j*4);
											record.add(filendType.readInt());

										}
										records.add(record);
									}
								}
							}
						}

						Collections.sort(records, new Comparator<List<Integer>>() {
							@Override
							public int compare(List<Integer> o1, List<Integer> o2) {
								try {
									return o1.get(0).compareTo(o2.get(0));
								} catch (NullPointerException e) {
									return 0;
								}
							}
						});

						int size = records.size();
						for(int j=0;j<size;j++){
							LinkedList<Integer> record = new LinkedList<Integer>();
							record = records.poll();
							int siz = record.size();
							for(int k=0;k<siz;k++){
								output.print(record.poll() +" ");
							}
							output.println();
						}
						filendType.close();
					}
				}
			}

			if (databaseInstruction.getCommand().equals("search")) {
				if (databaseInstruction.getType().equals("record")) {

					String fileName= databaseInstruction.getName() + ".txt";
					File f = new File(fileName);
					if(f.exists()){
						RandomAccessFile filendType = new RandomAccessFile(fileName, "rw");
						filendType.seek(0);
						int numberPage = filendType.readByte();
						filendType.seek(1); 
						int numberField = filendType.readByte();
						int recordHold = numberField*4+1; 
						int max = (recordHold*40+1)*10+2;
						for(int k=1;k<filendType.length() && k<max ;k+=40*recordHold+1){
							filendType.seek(k+1); 
							int numberRecords = filendType.readByte();
							int primaryKey = Integer.parseInt(databaseInstruction.getArgument(0));
							for(int l=0;l<=numberRecords*recordHold;l+=recordHold){
								if(filendType.length()>l+k+3){
									filendType.seek(l+k+3);
									int o = filendType.readInt();
									filendType.seek(l+k+2);
									int emptyBit = filendType.readByte(); 
									if(primaryKey == o && emptyBit==0){
										output.print(o +" ");
										for(int j=1;j<numberField;j++){
											filendType.seek(l+k+3+j*4);
											o = filendType.readInt();
											output.print(o +" ");
										}
										output.println();
										break;
									}
								}
							}
						}
						filendType.close();
					}
				}
			}

			if (databaseInstruction.getCommand().equals("delete")) {
				if (databaseInstruction.getType().equals("type")) {
					for(int k=3;k<dataDic.length();k+=148){
						dataDic.seek(k);
						String a = "";
						for(int s=0;s<11;s++){
							a += (char)dataDic.readByte();
						}
						a = a.substring(0, a.indexOf('#'));
						if(a.equals(databaseInstruction.getName())){
							k-=3;
							dataDic.seek(k);
							dataDic.writeByte(1);

							String f = databaseInstruction.getName() + ".txt";
							File file = new File(f);
							if(file.exists()){
								file.delete();
							}

							break;
						}		
					}
				} else {

					String fileName= databaseInstruction.getName() + ".txt";
					File f = new File(fileName);
					if(f.exists()){
						RandomAccessFile filendType = new RandomAccessFile(fileName, "rw");
						filendType.seek(0);
						int numberPage = filendType.readByte();
						filendType.seek(1); 
						int numberField = filendType.readByte();
						int recordHold = numberField*4+1; 
						int max = (recordHold*40+1)*10+2;
						for(int k=1;k<filendType.length() && k<max ;k+=40*recordHold+1){
							filendType.seek(k+1); 
							int numberRecords = filendType.readByte();
							int primaryKey = Integer.parseInt(databaseInstruction.getArgument(0));
							for(int l=0;l<=40*recordHold;l+=recordHold){
								if(filendType.length()>l+k+3){
									filendType.seek(l+k+3);
									int o = filendType.readInt();
									if(primaryKey == o){
										filendType.seek(l+k+2);
										filendType.writeByte(1);
										numberRecords--;
										filendType.seek(k+1); 
										filendType.writeByte(numberRecords);
										break;
									}
								}
							}
						}
						filendType.close();
					}
				}
			}

			if (databaseInstruction.getCommand().equals("update")) {
				if (databaseInstruction.getType().equals("record")) {

					String fileName= databaseInstruction.getName() + ".txt";
					File f = new File(fileName);
					if(f.exists()){
						RandomAccessFile filendType = new RandomAccessFile(fileName, "rw");
						filendType.seek(0);
						int numberPage = filendType.readByte();
						filendType.seek(1); 
						int numberField = filendType.readByte();
						int recordHold = numberField*4+1; 
						int max = (recordHold*40+1)*10+2;
						for(int k=1;k<filendType.length() && k<max ;k+=40*recordHold+1){
							filendType.seek(k+1); 
							int numberRecords = filendType.readByte();
							int primaryKey = Integer.parseInt(databaseInstruction.getArgument(0));
							for(int l=0;l<=numberRecords*recordHold;l+=recordHold){
								if(filendType.length()>l+k+3){
									filendType.seek(l+k+3);
									int o = filendType.readInt();
									filendType.seek(l+k+2);
									int emptyBit = filendType.readByte(); 
									if(primaryKey == o && emptyBit==0){
										for(int j=1;j<numberField;j++){
											filendType.seek(l+k+3+j*4);
											filendType.writeInt(Integer.parseInt(databaseInstruction.getArgument(j)));
										}
										break;
									}
								}
							}
						}
						filendType.close();
					}

				}
			}
		}
		
	}
}