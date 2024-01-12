package org.example;

public class Action implements Comparable<Action> {
    // depending on the matrix indexing convention - can be 0 or 1
    int indexing = 1;
    char type;
    int i;
    int j;
    int k;

    public Action(char type, int i, int j, int k) {
        this.type = type;
        this.i = i + indexing;
        this.j = j + indexing;
        this.k = k + indexing;
    }

    public Action(char type) {
        this.type = type;
        this.i = -1;
        this.j = -1;
        this.k = -1;
    }

    public boolean isDependent(Action other) {
        // 1) B use A value
        if ((this.type == 'A' && other.type == 'B') || (this.type == 'B' && other.type == 'A')) {
            return (this.i == other.i &&
                    this.k == other.k);
        }
        if ((this.type == 'B' && other.type == 'C') || (this.type == 'C' && other.type == 'B')) {
            // 2) C use B value
            if (this.i == other.i && this.j == other.j && this.k == other.k) return true;

            // 3) C modifies matrix fields used by B
            int iB, jB, iC, jC, kC;
            if (this.type == 'B') {
                iB = this.i; jB = this.j; iC = other.i; jC = other.j; kC = other.k;
            } else {
                iB = other.i; jB = other.j; iC = this.i; jC = this.j; kC = this.k;
            }
            return iB == kC && jB == jC && iC < iB;
        }
        // 4) A uses fields which could be previously modified by C ( A(i,k) -> multiplier = M[k][i] / M[i][i] )
        if ((this.type == 'A' && other.type == 'C') || (this.type == 'C' && other.type == 'A')) {
            int iA, kA, iC, jC, kC;
            if (this.type == 'A') {
                iA = this.i; kA = this.k; iC = other.i; jC = other.j; kC = other.k;
            } else {
                iA = other.i; kA = other.k; iC = this.i; jC = this.j; kC = this.k;
            }

            return iA == jC && (kC == iA || kC == kA) && iC < iA;
        }

        // 5) C using matrix fields modified by previous C
        if (this.type == 'C' && other.type == 'C') {
            int i1, j1, k1, i2, j2, k2;
            if (this.i < other.i) {
                i1 = this.i; j1 = this.j; k1 = this.k; i2 = other.i; j2 = other.j; k2 = other.k;
            } else {
                i1 = other.i; j1 = other.j; k1 = other.k; i2 = this.i; j2 = this.j; k2 = this.k;
            }
            return i1 != i2 && j1 == j2 && k1 == k2;
//            return k1 == k2 && j2 == j1;
        }
        return false;
    }

    public char getType() {
        return type;
    }
    public int getI() {
        return i;
    }
    public int getJ() {
        return j;
    }
    public int getK() {
        return k;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(type == '*') {
            return "*";
        } else {
            sb.append(type);
            sb.append("(");
            sb.append(i);
            if(type != 'A') {
                sb.append(",");
                sb.append(j);
            }
            sb.append(",");
            sb.append(k);
            sb.append(")");
            return sb.toString();
        }
    }

    @Override
    public int compareTo(Action o) {
        return Character.compare(this.type, o.type);
    }

//    @Override
//    public String toString() {
//        return type +
//                "(i=" + i +
//                ", k=" + k +
//                ", j=" + j +
//                ", value=" + value +
//                ')';
//    }
}
