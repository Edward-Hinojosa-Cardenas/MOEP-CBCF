import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

//Weighted Sum of Others Ratings
public class wsor {

	public static void main(String[] args) throws IOException {
		String sDS = "test_wsor_ieee"; // "test_wsor"; //"ml-100k";
		String iFold = "1";
		int nUsers = 8; //5; //943;
		int nItems = 5; //4; //1682;
		
		double[][] matrixR = new double[nUsers][nItems];
		double[][] matrixR_Predic = new double[nUsers][nItems];
		double[][] matrixWUser = new double[nUsers][nUsers];
		int user, item;
		double rank;
		
		double r_u, r_v, r_a;
		int i_r, i_a;
		double sum_nume;
		double sum_deno;
		double sum_deno_u;
		double sum_deno_v;
		double[] da_r = new double[nUsers];
		double pred;
		
		// Read Train
		String docuTrain = System.getProperty("user.dir") + File.separator + "datasets" + File.separator + sDS +  File.separator + "u" + iFold + ".base";
				
		try (BufferedReader br = Files.newBufferedReader(Paths.get(docuTrain))) {
			String sLine;
			String[] asData;
			while ((sLine = br.readLine()) != null) {
				asData = sLine.split("\t");
				user = Integer.parseInt(asData[0]);
				item = Integer.parseInt(asData[1]);
				rank = Double.parseDouble(asData[2]);
				matrixR[user - 1][item - 1] = rank;
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		
		for(int u = 0; u < nUsers - 1; u++) {
			for(int v = u + 1; v < nUsers; v++) {
				i_r = 0;
				r_u = r_v = sum_nume = sum_deno_u = sum_deno_v = 0.0;
				for(int i = 0; i < nItems; i++) {
					if(matrixR[u][i] > 0.0 && matrixR[v][i] > 0) {
						r_u = r_u + matrixR[u][i];
						r_v = r_v + matrixR[v][i];
						i_r++;
					}
				}
				r_u = r_u / i_r;
				r_v = r_v / i_r;
				
				for(int i = 0; i < nItems; i++) {
					if(matrixR[u][i] > 0.0 && matrixR[v][i] > 0) {
						sum_nume = sum_nume + (matrixR[u][i] - r_u) * (matrixR[v][i] - r_v);
						sum_deno_u = sum_deno_u + Math.pow(matrixR[u][i] - r_u, 2.0);
						sum_deno_v = sum_deno_v + Math.pow(matrixR[v][i] - r_v, 2.0);
					}
				}
				
				sum_deno = Math.sqrt(sum_deno_u) * Math.sqrt(sum_deno_v);
				if(sum_deno != 0) {
					matrixWUser[u][v] = sum_nume / sum_deno;
					matrixWUser[v][u] = sum_nume / sum_deno;
				} else {
					matrixWUser[u][v] = 0.0;
					matrixWUser[v][u] = 0.0;
				}
			}
			//System.out.println(u);
		}
		
		//System.out.println(matrixWUser[0][4]);
		//System.out.println(matrixWUser[2][6]);
		showMatrizDouble(matrixWUser);
		
		System.out.println("********************************************************");
		
		for(int u = 0; u < nUsers; u++) {
			for(int i = 0; i < nItems; i++) {
				for(int u1 = 0; u1 < nUsers; u1++) {
					da_r[u1] = 0.0;
				}
				for(int u1 = 0; u1 < nUsers; u1++) {
					r_a = 0.0;
					i_a = 0;
					for(int i1 = 0; i1 < nItems; i1++) {
						if(i != i1 && matrixR[u1][i1] > 0.0) {
							r_a = r_a + matrixR[u1][i1];
							i_a++;
						}
					}
					if(i_a != 0) {
						r_a = r_a / i_a;
					} else {
						r_a = 0.0;
					}
					da_r[u1] = r_a;
				}
				sum_nume = sum_deno = 0.0;
				for(int u1 = 0; u1 < nUsers; u1++) {
					if(u != u1 && matrixR[u1][i] > 0.0) {
						sum_nume = sum_nume + (matrixR[u1][i] - da_r[u1]) * matrixWUser[u][u1];
						sum_deno = sum_deno + Math.abs(matrixWUser[u][u1]);
					}
				}
				if(sum_deno != 0.0) {
					pred = da_r[u] + sum_nume / sum_deno;
				} else {
					pred = 0.0;
				}
				matrixR_Predic[u][i] = pred;
			}
			//System.out.println(u);
		}
		
		
		System.out.println(matrixR_Predic[0][3]);
		System.out.println(matrixR_Predic[7][2]);
		System.out.println();
		showMatrizDouble(matrixR_Predic);
		saveMatrizDouble(docuTrain, matrixR_Predic);
		//System.out.println(matrixR[524][299]);
		
		//System.out.println("END");
	}
	
	private static void saveMatrizDouble(String docuTrain, double[][] matrix) throws IOException {
		File matriz = new File(docuTrain.replace(".base", ".matriz"));
		if (matriz.exists()) {
			matriz.delete();
		}
		matriz.createNewFile();
		FileOutputStream fileOutput = new FileOutputStream(matriz);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutput));
		for (int r = 0; r < matrix.length; r++) {
			for (int c = 0; c < matrix[r].length - 1; c++) {
				bw.write(matrix[r][c] + " ");
			}
			bw.write(String.valueOf(matrix[r][matrix[r].length - 1]));
			bw.newLine();
		}
		bw.close();
	}

	private static void showMatrizDouble(double[][] matrix) {
		System.out.println("******** Prediction Matriz *********");
		DecimalFormat form = new DecimalFormat("########0.00");
		for (int r = 0; r < matrix.length; r++) {
			for (int c = 0; c < matrix[r].length; c++) {
				System.out.print(form.format(matrix[r][c]) + "\t");
			}
			System.out.println();
		}
	}

}
