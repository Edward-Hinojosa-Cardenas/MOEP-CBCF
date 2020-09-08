import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

//Memory-Based Collaborative Filtering Techniques
public class mbcf {

	public static void main(String[] args) {
		String sDS = "ml-100k";
		String sFold = "1";
		int nUser = 943;
		int nItem = 1682;
		
		int[][] test = new int[20000][3];
		int aux, user, item, rank, iUser, iItem;
		double[][] R_predict = new double[nUser][nItem];
		double delta = 4.0;
		double TruePositive = 0.0;
		double FalsePositive = 0.0;
		double FalseNegative = 0.0;
		double precision, recall, f1;
		
		String docuTest = System.getProperty("user.dir") + File.separator + "datasets" + File.separator + sDS +  File.separator + "u" + sFold + ".test";
		String docuMatriz = System.getProperty("user.dir") + File.separator + "datasets" + File.separator + sDS +  File.separator + "u" + sFold + ".matriz";
		
		try (BufferedReader br = Files.newBufferedReader(Paths.get(docuTest))) {
			String sLine;
			String[] asData;
			aux = 0;
			sLine = br.readLine();
			while (sLine != null && !sLine.equals("")) {
				asData = sLine.split("\t");
				user = Integer.parseInt(asData[0]);
				item = Integer.parseInt(asData[1]);
				rank = Integer.parseInt(asData[2]);
				test[aux][0] = user - 1;
				test[aux][1] = item - 1;
				test[aux][2] = rank;
				aux++;
				sLine = br.readLine();
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		
		try (BufferedReader br = Files.newBufferedReader(Paths.get(docuMatriz))) {
			String sLine;
			String[] asData;
			aux = 0;
			sLine = br.readLine();
			while (sLine != null && !sLine.equals("")) {
				asData = sLine.split(" ");
				for(int i = 0; i < asData.length; i++) {
					R_predict[aux][i] = Double.parseDouble(asData[i]);
				}
				aux++;
				sLine = br.readLine();
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		
		for(int l = 0; l < test.length; l++) {
			iUser = test[l][0];
			iItem = test[l][1];
			
			if(R_predict[iUser][iItem] >= delta && test[l][2] >= delta) {
				TruePositive++;
			}
			if(R_predict[iUser][iItem] < delta && test[l][2] < delta) {
				FalsePositive++;
			}
			if(R_predict[iUser][iItem] < delta && test[l][2] >= delta) {
				FalseNegative++;
			}
		}
		
		if(TruePositive + FalsePositive != 0.0) {
			precision = TruePositive / (TruePositive + FalsePositive);
		} else {
			precision = 0.0;
		}
		if(TruePositive + FalseNegative != 0.0) {
			recall = TruePositive / (TruePositive + FalseNegative);
		} else {
			recall = 0.0;
		}
		if(precision + recall != 0.0) {
			f1 = 2 * precision * recall / (precision + recall);
		} else {
			f1 = 0.0;
		}
		
		System.out.println(precision + " - " + recall + " - " + f1);
	}
}
