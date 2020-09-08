import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

class ArrayIntegerDouble
{
    public int[] intValues;
    public double[] doubleValues;

    public ArrayIntegerDouble(int cantInteger, int cantDouble)
    {
    	intValues = new int[cantInteger];
    	doubleValues = new double[cantDouble];
    }
    public Object getVal(int i) {
    	if(i < intValues.length) {
    		return intValues[i]; 
    	} else {
    		return doubleValues[i - intValues.length]; 
    	}
    }
    
    public int getClass(int u) {
    	return intValues[u];
    }
    public double getVari(int i) {
    	return doubleValues[i];
    }
    public void setClass(int pos, int c) {
    	intValues[pos] = c;
    }
    public void setVari(int pos, double v) {
    	doubleValues[pos] = v;
    }
    public void clear() {
    	for(int i = 0; i < intValues.length; i++) {
    		intValues[i] = 0;
    	}
    	for(int i = 0; i < doubleValues.length; i++) {
    		doubleValues[i] = 0.0;
    	}
    }
    public int sizeClass() {
    	return intValues.length;
    }
    public int sizeVari() {
    	 return doubleValues.length;
    }
}

class classIndi_MOEP {
	ArrayIntegerDouble indi;
	double precision, recall, f1;
	int front;
	int user;
	int vari;
	int classes;
	double miniValuVari;
	double maxiValuVari;
	double indiLenght;
	classIndi_MOEP(int _nUsers, int _nVari, int _nClass, double _miniValuVari, double _maxiValuVari) {
		indi = new ArrayIntegerDouble(_nUsers, _nVari);
		user = _nUsers;
		classes = _nClass;
		vari = _nVari;
		indiLenght = _nUsers + _nVari;
		miniValuVari = _miniValuVari;
		maxiValuVari = _maxiValuVari;
	}
	
	public void create_Indi() {
		Random r = new Random();
		for(int i = 0; i < user; i++) {
			indi.intValues[i] = r.nextInt(classes) + 1;
		}
		for(int i = 0; i < vari; i++) {
			if(vari < 3) {
				indi.doubleValues[i] = r.nextDouble() * (maxiValuVari - miniValuVari) + miniValuVari;
			} else {
				indi.doubleValues[i] = r.nextDouble();
			}
		}
		front = -1;
	}
	
	public void calculateObjectives (int[][] R_CBCF, double[][] R_predict, int[][] test) {
		int iUserClass;
		double AverUserClass_Item;
		int iAverUserClass_Item;
		int iItem;
		int iUser;
		double alpha, beta, gamma, rho, sigma;
		double delta = 4.0;
		double ftp = 0.0;
		double ffp = 0.0;
		double ffn = 0.0;
		double predict;
		
		alpha = indi.getVari(0);
		beta = indi.getVari(1);
		gamma = indi.getVari(2);
		rho = indi.getVari(3);
		sigma = indi.getVari(4);
		
		for(int l = 0; l < test.length; l++) {
			iUser = test[l][0];
			iItem = test[l][1];
			iUserClass = indi.getClass(iUser);
			AverUserClass_Item = 0.0;
			iAverUserClass_Item = 0;
			for(int u = 0; u < user; u++) {
				if(indi.getClass(u) == iUserClass && R_CBCF[u][iItem] > 0) {
					AverUserClass_Item = AverUserClass_Item + R_CBCF[u][iItem];
					iAverUserClass_Item++;
				}
			}
			if(iAverUserClass_Item > 0) {
				AverUserClass_Item = AverUserClass_Item / iAverUserClass_Item;
			} else {
				AverUserClass_Item = 0.0;
			}
			//System.out.println(AverUserClass_Item);
			
			
			/*if(test[l][2] >= delta) {
				System.out.print(l + ")" + test[l][2] + " - R - ");
			} else {
				System.out.print(l + ")" + test[l][2] + " - N - ");
			}
			
			if(AverUserClass_Item >= gamma) {
				if(R_predict[iUser][iItem] >= beta) {
					System.out.println("R");
				} else {
					System.out.println("N");
				}
			}
			else {
				if(R_predict[iUser][iItem] >= alpha) {
					System.out.println("R");
				} else {
					System.out.println("N");
				}
			}*/
			
			AverUserClass_Item = rho * AverUserClass_Item;
			predict = sigma * R_predict[iUser][iItem];
			
			if(AverUserClass_Item >= gamma && predict >= beta && test[l][2] >= delta) {
				ftp++;
			} else if (AverUserClass_Item < gamma && predict >= alpha && test[l][2] >= delta) {
				ftp++;
			}
			if(AverUserClass_Item >= gamma && predict >= beta && test[l][2] < delta) {
				ffp++;
			} else if (AverUserClass_Item < gamma && predict >= alpha && test[l][2] < delta) {
				ffp++;
			}
			if(AverUserClass_Item >= gamma && R_predict[iUser][iItem] < beta && test[l][2] >= delta) {
				ffn++;
			} else if (AverUserClass_Item < gamma && predict < alpha && test[l][2] >= delta) {
				ffn++;
			}
		}
		if(ftp + ffp != 0.0) {
			precision = ftp / (ftp + ffp);
		} else {
			precision = 0.0;
		}
		if(ftp + ffn != 0.0) {
			recall = ftp / (ftp + ffn);
		} else {
			recall = 0.0;
		}
		if(precision + recall != 0.0) {
			f1 = 2 * precision * recall / (precision + recall);
		} else {
			f1 = 0.0;
		}
		front = -1;
	}
	
	public void show_Indi() {
		for(int i = 0; i < indiLenght; i++) {
			System.out.print(indi.getVal(i) + " ");
		}
	}
}

class classMOEP {
	classIndi_MOEP[] parents;
	classIndi_MOEP[] child;
	classIndi_MOEP[] extra_population;
	int nIter, nUser, nVari, nIndi, nClass;
	Random rn;
    
	public classMOEP(int _nIndi, int _nUser, int _nVari, int _nClass, double _miniValuVari, double _maxiValuVari) {
		nIndi = _nIndi;
		nUser = _nUser;
		nVari = _nVari;
		nClass = _nClass;
		parents = new classIndi_MOEP[_nIndi];
		for(int y = 0; y < _nIndi; y++) {
			parents[y] = new classIndi_MOEP(_nUser, _nVari, _nClass, _miniValuVari, _maxiValuVari);
		}
		child = new classIndi_MOEP[_nIndi];
		for(int y = 0; y < _nIndi; y++) {
			child[y] = new classIndi_MOEP(_nUser, _nVari, _nClass, _miniValuVari, _maxiValuVari);
		}
		extra_population = new classIndi_MOEP[2 * _nIndi];
		for(int y = 0; y < 2 * _nIndi; y++) {
			extra_population[y] = new classIndi_MOEP(_nUser, _nVari, _nClass, _miniValuVari, _maxiValuVari);
		}
		nIter = 0;
		rn = new Random();
	}
	
	public void create_initial_population(int[][] R_CBCF, double[][] R_predict, int[][] test) {
		ArrayList<Double> prec = new ArrayList<Double>();
		ArrayList<Double> reca = new ArrayList<Double>();
		boolean diferent;
		for(int x = 0; x < nIndi; x++) {
			diferent = true;
			parents[x].create_Indi();
			parents[x].calculateObjectives(R_CBCF, R_predict, test);
			for(int i = 0; i < prec.size(); i++) {
				if(parents[x].precision == prec.get(i) && parents[x].recall == reca.get(i)) {
					diferent = false;
					break;
				}
			}
			if(!diferent) {
				x--;
			} else {
				prec.add(parents[x].precision);
				reca.add(parents[x].recall);	
			}
		}
	}
	
	public void replace_population() {
		for(int x = 0; x < nIndi; x++) {
			for(int y = 0; y < extra_population[x].indi.sizeClass(); y++) {
				parents[x].indi.setClass(y, extra_population[x].indi.getClass(y));
			}
			for(int y = 0; y < extra_population[x].indi.sizeVari(); y++) {
				parents[x].indi.setVari(y, extra_population[x].indi.getVari(y));
			}
		}
		for(int x = 0; x < nIndi; x++) {
			child[x].indi.clear();
			extra_population[x].indi.clear();
		}
	}
	
	public void show_population() {
		for(int x = 0; x < nIndi; x++) {
			System.out.print((x+1) + ") ");
			parents[x].show_Indi();
			System.out.println();
		}
	}
	
	public void calculte_objectives (int[][] R_CBCF, double[][] R_predict, int[][] test) {
		for(int x = 0; x < nIndi; x++) {
			parents[x].calculateObjectives(R_CBCF, R_predict, test);
		}
	}
	
	public void show_population_fitness() {
		for(int x = 0; x < nIndi; x++) {
			System.out.print((x+1) + ") ");
			System.out.print(parents[x].precision + " - " + parents[x].recall + " - " + parents[x].f1);
			System.out.println();
		}
	}
	
	public void next_population() {
		ArrayList<Integer> nextPopu = new ArrayList<>();
		int iFront = 1;
		int iCantFront;
		double[][] mDista;
		double maxiDista;
		int diff;
		int x1 = 0;
		int y1 = 0;
		double p1, r1, p2, r2, p, r;
		int maxiX;
		while(nextPopu.size() < nIndi) {
			iCantFront = 0;
			for(int i = 0; i < extra_population.length; i++) {
				if(extra_population[i].front == iFront) {
					iCantFront++;
				}
			}
			//System.out.println("In Front: " + iCantFront + "; Indi: " + (nextPopu.size() + iCantFront));
			if(nextPopu.size() + iCantFront <= nIndi) {
				for(int i = 0; i < extra_population.length; i++) {
					if(extra_population[i].front == iFront) {
						nextPopu.add(i);
					}
				}
				iFront++;
			}
			else {
				diff = nIndi - nextPopu.size();
				mDista = new double[iCantFront][iCantFront];
				for(int x = 0; x < extra_population.length; x++) {
					if(extra_population[x].front == iFront) {
						for(int y = 0; y < extra_population.length; y++) {
							if(extra_population[y].front == iFront) {
								p1 = extra_population[x].precision;
								p2 = extra_population[y].precision;
								r1 = extra_population[x].recall;
								r2 = extra_population[y].recall;
								p = p1 - p2;
								r = r1 - r2;
								mDista[x1][y1] = Math.sqrt(Math.pow(p, 2) + Math.pow(r, 2));
								y1++;
							}
						}
						y1 = 0;
						x1++;
					}
				}
			
				for(int d = 0; d < diff; d++) {
					maxiDista = -1.0;
					x1 = y1 = 0;
					maxiX = -1;
					for(int x = 0; x < extra_population.length; x++) {
						if(extra_population[x].front == iFront) {
							for(int y = 0; y < extra_population.length; y++) {
								if(extra_population[y].front == iFront) {
									if(x != y && mDista[x1][y1] > maxiDista && !nextPopu.contains(x) && !nextPopu.contains(y)) {
										maxiDista = mDista[x1][y1];
										maxiX = x;
									}
									y1++;
								}
							}
							y1 = 0;
							x1++;
						}
					}
					if(!nextPopu.contains(maxiX)) {
						nextPopu.add(maxiX);
					}
				}
			}
		}
		for(int i = 0; i < nextPopu.size(); i++) {
			for(int y = 0; y < extra_population[nextPopu.get(i)].indi.sizeClass(); y++) {
				parents[i].indi.setClass(y, extra_population[nextPopu.get(i)].indi.getClass(y));
			}
			for(int y = 0; y < extra_population[nextPopu.get(i)].indi.sizeVari(); y++) {
				parents[i].indi.setVari(y, extra_population[nextPopu.get(i)].indi.getVari(y));
			}
			parents[i].precision = extra_population[nextPopu.get(i)].precision;
			parents[i].recall = extra_population[nextPopu.get(i)].recall;
			parents[i].f1 = extra_population[nextPopu.get(i)].f1;
			parents[i].front = -1;
		}
	}
	
	public void show_result(int nClass) {
		for(int c = 0; c < nClass; c++) {
			System.out.print((c + 1) + ") ");
			for(int i = 0; i < parents[0].indi.sizeClass(); i++) {
				if(parents[0].indi.getClass(i) == c + 1) {
					System.out.print((i + 1) + " - ");
				}
			}
			System.out.println();
		}
	}
	
	public void show_new_population_fitness() {
		for(int x = 0; x < nIndi; x++) {
			System.out.print((x+1) + ") ");
			System.out.print(child[x].precision + " - " + child[x].recall + " - " + child[x].f1);
			System.out.println();
		}
	}
	
	public void show_indi (int iIndi) {
		parents[iIndi].show_Indi();
		System.out.println();
	}
	
	public void merge_populations() {
		for(int i = 0; i < nIndi; i++) {
			for(int y = 0; y < parents[i].indi.sizeClass(); y++) {
				extra_population[i].indi.setClass(y, parents[i].indi.getClass(y));
			}
			for(int y = 0; y < parents[i].indi.sizeVari(); y++) {
				extra_population[i].indi.setVari(y, parents[i].indi.getVari(y));
			}
			extra_population[i].precision = parents[i].precision;
			extra_population[i].recall = parents[i].recall;
			extra_population[i].f1 = parents[i].f1;
			extra_population[i].front = -1;
		}
		
		for(int i = 0; i < nIndi; i++) {
			for(int y = 0; y < child[i].indi.sizeClass(); y++) {
				extra_population[i + nIndi].indi.setClass(y, child[i].indi.getClass(y));
			}
			for(int y = 0; y < child[i].indi.sizeVari(); y++) {
				extra_population[i + nIndi].indi.setVari(y, child[i].indi.getVari(y));
			}
			extra_population[i + nIndi].precision = child[i].precision;
			extra_population[i + nIndi].recall = child[i].recall;
			extra_population[i + nIndi].f1 = child[i].f1;
			extra_population[i + nIndi].front = -1;
		}
	}

	public void build_fronts() {
		int iFront = 1;
		boolean isInFront;
		boolean allInFront = false;
		ArrayList<Integer> arFront = new ArrayList<Integer>();
		while(!allInFront) {
			arFront.clear();
			for(int i = 0; i < 2 * nIndi; i++) {
				isInFront = true;
				if(extra_population[i].front == -1) {
					for(int iA = 0; iA < 2 * nIndi; iA++) {
						if(extra_population[iA].front == -1) {
							if(i != iA) {
								if((extra_population[i].precision < extra_population[iA].precision && extra_population[i].recall < extra_population[iA].recall) 
										|| (extra_population[i].precision < extra_population[iA].precision && extra_population[i].recall == extra_population[iA].recall)
										|| (extra_population[i].precision == extra_population[iA].precision && extra_population[i].recall < extra_population[iA].recall)) {
									isInFront = false;
									break;
								}
							}
						}
					}
				} else {
					isInFront = false;
				}
				if(isInFront) {
					arFront.add(i);
				}
			}
			for(int i = 0; i < arFront.size(); i++) {
				extra_population[arFront.get(i)].front = iFront;
			}
			/*System.out.println(iFront);
			for(int i = 0; i < 2 * nIndi; i++) {
				System.out.println(extra_population[i].precision + "\t" + extra_population[i].recall + "\t" + extra_population[i].front);
			}*/
			iFront++;
			allInFront = true;
			for(int i = 0; i < 2 * nIndi; i++) {
				if(extra_population[i].front == -1) {
					allInFront = false;
					break;
				}
			}
		}
	}
}

public class moepcbcf {

	public static void main(String[] args) throws IOException {
		String sDS = "ml-100k";
		String sFold = "1";
		
		int nIter = 100; 
		int sizeP = 50;
		int nUser = 943;
		int nItem = 1682;
		int nVari = 5;
		int C_max = 10;
		Random rn = new Random();
		double rMuta;
		
		int[][] R_CBCF = new int[nUser][nItem];
		double[][] R_predict = new double[nUser][nItem];
		
		double minRank = 1.0;
		double maxRank = 5.0;
		
		ArrayList<Double> prec = new ArrayList<Double>();
		ArrayList<Double> reca = new ArrayList<Double>();
		boolean diferent;
		
		int[][] test = new int[20000][3];
		int aux, user, item, rank;
		String docuTest = System.getProperty("user.dir") + File.separator + "datasets" + File.separator + sDS +  File.separator + "u" + sFold + ".test";
		String docuTrain = System.getProperty("user.dir") + File.separator + "datasets" + File.separator + sDS +  File.separator + "u" + sFold + ".base";
		String docuMatriz = System.getProperty("user.dir") + File.separator + "datasets" + File.separator + sDS +  File.separator + "u" + sFold + ".matriz";
		
		try (BufferedReader br = Files.newBufferedReader(Paths.get(docuTrain))) {
			String sLine;
			String[] asData;
			aux = 0;
			sLine = br.readLine();
			while (sLine != null && !sLine.equals("")) {
				asData = sLine.split("\t");
				user = Integer.parseInt(asData[0]);
				item = Integer.parseInt(asData[1]);
				rank = Integer.parseInt(asData[2]);
				R_CBCF[user - 1][item - 1] = rank;
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
		
		File path_results = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + sDS + File.separator +  "fold_" + sFold + File.separator + "iterations" + File.separator);
		if(!path_results.exists()){
			path_results.mkdirs();
		}
		
		System.out.println("Parameters:");
		System.out.println("- Dataset: " + sDS);
		System.out.println("- Fold: " + sFold);
		System.out.println("- Population size: " + sizeP);
		System.out.println("- Number of users: " + nUser);
		System.out.println("- Number of items: " + nItem);
		System.out.println("- Number of classes: " + C_max);
		System.out.println("- Number of iterations: " + nIter);
		System.out.println();
		
		classMOEP ep = new classMOEP(sizeP, nUser, nVari, C_max, minRank, maxRank);
        
		//===================
        System.out.println("create a initial population of parents");
        ep.create_initial_population(R_CBCF, R_predict, test);
		
		printSolutions(ep.parents, 0, sDS, sFold);
		//showMejora(ep.population);
		
		for(int ite = 0; ite < nIter; ite++) {
			prec.clear();
			reca.clear();
			for(int y = 0; y < ep.parents.length; y++) {
				prec.add(ep.parents[y].precision);
				reca.add(ep.parents[y].recall);
			}
			
			System.out.println("****** Iteration " + (ite + 1) + " ******");
			System.out.println("create a population of child");
			
			for(int i = 0; i < sizeP; i++) {
				//ep.population[i].show_Indi();
				//System.out.println();
				for(int y = 0; y < ep.parents[i].indi.sizeClass(); y++) {
					ep.child[i].indi.setClass(y, ep.parents[i].indi.getClass(y));
				}
				for(int y = 0; y < ep.parents[i].indi.sizeVari(); y++) {
					ep.child[i].indi.setVari(y, ep.parents[i].indi.getVari(y));
				}
				rMuta = rn.nextDouble();
				if(rMuta < 0.1) {
					for(int y = 0; y < ep.parents[i].indi.sizeClass(); y++) {
						if(rn.nextDouble() < 0.1) {
							ep.child[i].indi.setClass(y, rn.nextInt(C_max) + 1);
						}
					}
				} else if (rMuta >= 0.1 && rMuta < 0.2) {
					for(int y = 0; y < ep.parents[i].indi.sizeClass(); y++) {
						if(rn.nextDouble() < 0.2) {
							ep.child[i].indi.setClass(y, rn.nextInt(C_max) + 1);
						}
					}
				} else if (rMuta >= 0.2 && rMuta < 0.3) {
					for(int y = 0; y < ep.parents[i].indi.sizeClass(); y++) {
						if(rn.nextDouble() < 0.3) {
							ep.child[i].indi.setClass(y, rn.nextInt(C_max) + 1);
						}
					}
				} else if (rMuta >= 0.3 && rMuta < 0.4) {
					for(int y = 0; y < ep.parents[i].indi.sizeClass(); y++) {
						if(rn.nextDouble() < 0.4) {
							ep.child[i].indi.setClass(y, rn.nextInt(C_max) + 1);
						}
					}
				} else if (rMuta >= 0.4 && rMuta < 0.5) {
					for(int y = 0; y < ep.parents[i].indi.sizeClass(); y++) {
						if(rn.nextDouble() < 0.5) {
							ep.child[i].indi.setClass(y, rn.nextInt(C_max) + 1);
						}
					}
				} else if (rMuta >= 0.5 && rMuta < 0.6) {
					ep.child[i].indi.setVari(0, rn.nextDouble() * (maxRank - minRank) + minRank);
				} else if (rMuta >= 0.6 && rMuta < 0.7) {
					ep.child[i].indi.setVari(1, rn.nextDouble() * (maxRank - minRank) + minRank);
				} else if (rMuta >= 0.7 && rMuta < 0.8) {
					ep.child[i].indi.setVari(2, rn.nextDouble() * (maxRank - minRank) + minRank);
				} else if (rMuta >= 0.8 && rMuta < 0.9) {
					ep.child[i].indi.setVari(3, rn.nextDouble());
				} else if (rMuta >= 0.9) {
					ep.child[i].indi.setVari(4, rn.nextDouble());
				}
				
				//System.out.println("Random: " + rMuta);
				//ep.new_population[i].show_Indi();
				//System.out.println();
				ep.child[i].calculateObjectives(R_CBCF, R_predict, test);
				
				diferent = true;
				for(int s = 0; s < prec.size(); s++) {
					if(ep.child[i].precision == prec.get(s) && ep.child[i].recall == reca.get(s)) {
						diferent = false;
						break;
					}
				}
				if(!diferent) {
					i--;
				} else {
					prec.add(ep.child[i].precision);
					reca.add(ep.child[i].recall);	
				}
			}
			
			System.out.println("merge parents and child populations");
			ep.merge_populations();
			
			System.out.println("build pareto fronts");
			ep.build_fronts();
			
			System.out.println("select next parents population");
			ep.next_population();
			
			System.out.println("print parents populations");
			printSolutions(ep.parents, ite + 1, sDS, sFold);
			
			showResults(ep.parents);
			
			System.out.println();
		}
	}

	private static void showResults(classIndi_MOEP[] population) {
		for(int i = 0; i < population.length; i++) {
    		if(population[i].precision > 0.6448 && population[i].recall > 0.8730 && population[i].f1 > 0.7418) {
    			System.out.println(population[i].indi.getVari(2) + " - " + population[i].indi.getVari(0) + " - " + population[i].indi.getVari(1) + " - " + population[i].indi.getVari(3) + " - " + population[i].indi.getVari(4) + " --- " + population[i].precision + " - " + population[i].recall + " - " + population[i].f1);
			//System.out.println(population[i].precision + " - " + population[i].recall + " - " + population[i].f1);
	    	}
		}
	}

	private static void printSolutions(classIndi_MOEP[] population, int iter, String sDS, String sFold) throws IOException {
		
		XYDataset dataset = createDataset(population);
		
		JFreeChart chart = ChartFactory.createScatterPlot("Precision vs. Recall", "recall", "precision", dataset);
		
		XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesPaint(0, Color.blue);
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setRange(0.00, 1.00);
        domain.setTickUnit(new NumberTickUnit(0.05));
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setRange(0.0, 1.0);
        range.setTickUnit(new NumberTickUnit(0.05));

	    BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = image.createGraphics();

	    g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
	    Rectangle r = new Rectangle(0, 0, 1200, 800);
	    chart.draw(g2, r);
	    File f = new File(System.getProperty("user.dir") + File.separator + "results" + File.separator + sDS + File.separator +  "fold_" + sFold + File.separator + "iterations" + File.separator + "iteration_" + iter + ".png");

	    BufferedImage chartImage = chart.createBufferedImage(1200, 800, null); 
	    ImageIO.write(chartImage, "png", f ); 
		
	}
	
	private static XYDataset createDataset(classIndi_MOEP[] population) {
	    XYSeriesCollection dataset = new XYSeriesCollection();
	    
	    XYSeries serie = new XYSeries("Solutions");
	    
	    for(int i = 0; i < population.length; i++) {
	    	serie.add(population[i].precision, population[i].recall);
	    }
	    
	    dataset.addSeries(serie);
	    return dataset;
	}
}
