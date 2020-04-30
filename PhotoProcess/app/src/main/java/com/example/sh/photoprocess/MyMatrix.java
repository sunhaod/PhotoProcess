package com.example.sh.photoprocess;

public class MyMatrix {
    float[][] matrix;
    int n, m;
    public MyMatrix(int n,int m) {
        this.n = n;
        this.m = m;
        matrix = new float[n][m];
    }
    public MyMatrix(MyMatrix a) {
        this.n = a.n;
        this.m = a.m;
        for(int i = 0; i < n; i ++)
            for(int j = 0; j < m; j ++){
                this.matrix[i][j] = a.matrix[i][j];
            }
    }
}
