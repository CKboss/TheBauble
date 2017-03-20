class ExpressionTree {

    private String value;
    private GBaseClass ast;
    private ExpressionTree leftChild, rightChild, parent;

    /************************************************/


    // 基本类型
    abstract class GBaseClass {

        protected String name, value, type;

        GBaseClass(String type, String name, String value) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        GBaseClass() {
        }

        abstract public GBaseClass particalGradient(GBaseClass partical);

        abstract public String expression();
    }

    // 常数
    class GConstant extends GBaseClass {

        GConstant(String value) {
            this.type = "CONSTANT";
            this.value = value;
            this.name = "GConstant";
        }

        @Override
        public GBaseClass particalGradient(GBaseClass partical) {
            return new GConstant("0");
        }

        @Override
        public String expression() {
            return value;
        }
    }

    // 变量
    class GVariable extends GBaseClass {

        GVariable(String name) {
            this.name = name;
            this.value = null;
            this.type = "VARIABLE";
        }

        @Override
        public GBaseClass particalGradient(GBaseClass partical) {
            if (partical.name == this.name) {
                return new GConstant("1");
            }
            return new GConstant("0");
        }

        @Override
        public String expression() {
            return this.name;
        }
    }

    // 操作符
    class GOperation extends GBaseClass {

        protected GBaseClass left, right;
        protected String operType;

        GOperation(String operType, GBaseClass left, GBaseClass right) {
            this.operType = operType;
            this.left = left;
            this.right = right;

            this.name = "Operation_" + operType;
            this.type = "OPERATION";
        }

        @Override
        public GOperation particalGradient(GBaseClass partical) {

            if (partical.type.equals("VARIABLE") == false) {
                System.out.println("Not Variable");
                return null;
            }

            if (this.operType.equals("add") || this.operType.equals("minus")) {
                return new GOperation(this.operType,
                        this.left.particalGradient(partical),
                        this.right.particalGradient(partical));
            } else if (this.operType.equals("mult")) {
                GOperation part1 = new GOperation("mult", this.left.particalGradient(partical), this.right);
                GOperation part2 = new GOperation("mult", this.left, this.right.particalGradient(partical));
                return new GOperation("add", part1, part2);
            } else if (this.operType.equals("sin")) {
                GOperation part1 = new GOperation("cos", this.left, null);
                return new GOperation("mult", this.left.particalGradient(partical), part1);
            } else if (this.operType.equals("cos")) {
                GOperation part1 = new GOperation("mult", new GConstant("-1"), this.left.particalGradient(partical));
                GOperation part2 = new GOperation("sin", this.left, null);
                return new GOperation("mult", part1, part2);
            } else if (this.operType.equals("exp")) {
                return new GOperation("mult", this.left.particalGradient(partical), this);
            }

            return null;
        }

        @Override
        public String expression() {
            /*
            if(this.operType.equals("add")) {
                return this.left.expression() + " + " + this.right.expression();
            }
            else if(this.operType.equals("minus")) {
                return this.left.expression() + " - " + this.right.expression();
            }
            else if(this.operType.equals("mult")) {
                return " ( " + this.left.expression() + " ) * ( " + this.right.expression() + " ) ";
            }
            else if(this.operType.equals("exp")) {
                return " exp( "+this.left.expression() + " ) ";
            }
            else if(this.operType.equals("sin")) {
                return " sin( "+this.left.expression() + " ) ";
            }
            else if(this.operType.equals("cos")) {
                return " cos( "+this.left.expression() + " ) ";
            }
            */
            if (this.operType.equals("add")) {
                return "add(" + this.left.expression() + "," + this.right.expression() + ")";
            } else if (this.operType.equals("minus")) {
                return "minus(" + this.left.expression() + "," + this.right.expression() + ")";
            } else if (this.operType.equals("mult")) {
                return "mult(" + this.left.expression() + "," + this.right.expression() + ")";
            } else if (this.operType.equals("exp")) {
                return "exp(" + this.left.expression() + ")";
            } else if (this.operType.equals("sin")) {
                return "sin(" + this.left.expression() + ")";
            } else if (this.operType.equals("cos")) {
                return "cos(" + this.left.expression() + ")";
            }
            return null;
        }

    }


    /************************************************/


    ExpressionTree() {
        value = null;
        leftChild = rightChild = parent = null;
    }

    // Constructor
    /* Arguments: String s: Value to be stored in the node
                  ExpressionTree l, r, p: the left child, right child, and parent of the node to created
       Returns: the newly created ExpressionTree
    */
    ExpressionTree(String s, ExpressionTree l, ExpressionTree r, ExpressionTree p) {
        value = s;
        leftChild = l;
        rightChild = r;
        parent = p;
    }

    /* Basic access methods */
    String getValue() {
        return value;
    }

    ExpressionTree getLeftChild() {
        return leftChild;
    }

    ExpressionTree getRightChild() {
        return rightChild;
    }

    ExpressionTree getParent() {
        return parent;
    }


    /* Basic setting methods */
    void setValue(String o) {
        value = o;
    }

    // sets the left child of this node to n
    void setLeftChild(ExpressionTree n) {
        leftChild = n;
        n.parent = this;
    }

    // sets the right child of this node to n
    void setRightChild(ExpressionTree n) {
        rightChild = n;
        n.parent = this;
    }


    // Returns the root of the tree describing the expression s
    // Watch out: it makes no validity checks whatsoever!
    ExpressionTree(String s) {
        // check if s contains parentheses. If it doesn't, then it's a leaf
        if (s.indexOf("(") == -1) setValue(s);
        else {  // it's not a leaf
            /* break the string into three parts: the operator, the left operand,
               and the right operand. ***/
            setValue(s.substring(0, s.indexOf("(")));
            // delimit the left operand 2008
            int left = s.indexOf("(") + 1;
            int i = left;
            int parCount = 0;
            // find the comma separating the two operands
            while (parCount >= 0 && !(s.charAt(i) == ',' && parCount == 0)) {
                if (s.charAt(i) == '(') parCount++;
                if (s.charAt(i) == ')') parCount--;
                i++;
            }
            int mid = i;
            if (parCount < 0) mid--;

            // recursively build the left subtree
            setLeftChild(new ExpressionTree(s.substring(left, mid)));

            if (parCount == 0) {
                // it is a binary operator
                // find the end of the second operand.F13
                while (!(s.charAt(i) == ')' && parCount == 0)) {
                    if (s.charAt(i) == '(') parCount++;
                    if (s.charAt(i) == ')') parCount--;
                    i++;
                }
                int right = i;
                setRightChild(new ExpressionTree(s.substring(mid + 1, right)));
            }
        }

    }

    // Returns a copy of the subtree rooted at this node... 2014
    ExpressionTree deepCopy() {
        ExpressionTree n = new ExpressionTree();
        n.setValue(getValue());
        if (getLeftChild() != null) n.setLeftChild(getLeftChild().deepCopy());
        if (getRightChild() != null) n.setRightChild(getRightChild().deepCopy());
        return n;
    }

    // Returns a String describing the subtree rooted at a certain node.
    public String toString() {
        String ret = value;
        if (getLeftChild() == null) return ret;
        else ret = ret + "(" + getLeftChild().toString();
        if (getRightChild() == null) return ret + ")";
        else ret = ret + "," + getRightChild().toString();
        ret = ret + ")";
        return ret;
    }

    // Returns the value of the expression rooted at a given node
    // when x has a certain value
    double evaluate(double x) {
        // WRITE YOUR CODE HERE

        // AND CHANGE THIS RETURN STATEMENT
        double ret = this.DG(this, x);
        return ret;
    }

    /* returns the root of a new expression tree representing the derivative of the
       original expression */
    ExpressionTree differentiate() {
        // WRITE YOUR CODE HERE

        // AND CHANGE THIS RETURN STATEMENT
        /// !!!!!!!!!1
        this.ast = this.buildAST(this);
        GBaseClass ast = this.ast;
        GVariable X = new GVariable("x");
        GOperation DX = (GOperation) ast.particalGradient(X);

        return new ExpressionTree(DX.expression());
    }

    double DG(ExpressionTree root, double x) {

        if (root == null) {
            return -Double.MAX_VALUE / 2;
        }

        double leftvalue = DG(root.leftChild, x);
        double rightvalue = DG(root.rightChild, x);

        if (root.value.equals("x")) {
            return x;
        } else if (root.value.equals("mult")) {
            return leftvalue * rightvalue;
        } else if (root.value.equals("add")) {
            return leftvalue + rightvalue;
        } else if (root.value.equals("minus")) {
            return leftvalue - rightvalue;
        } else if (root.value.equals("sin")) {
            return Math.sin(leftvalue);
        } else if (root.value.equals("cos")) {
            return Math.cos(leftvalue);
        } else if (root.value.equals("exp")) {
            return Math.exp(leftvalue);
        } else {
            return Double.valueOf(root.value);
        }
    }

    GBaseClass buildAST(ExpressionTree root) {

        GBaseClass left = null;
        GBaseClass right = null;

        if (root.leftChild != null) {
            left = buildAST(root.leftChild);
        }
        if (root.rightChild != null) {
            right = buildAST(root.rightChild);
        }

        if (root.value.equals("x")) {
            return new GVariable("x");
        } else if (root.value.equals("add") || root.value.equals("mult") || root.value.equals("minus")) {
            return new GOperation(root.value, left, right);
        } else if (root.value.equals("sin") || root.value.equals("cos") || root.value.equals("exp")) {
            return new GOperation(root.value, left, null);
        } else {
            return new GConstant(root.value);
        }
    }

    void showTree() {

        System.out.println(" --> ");
        System.out.println("value: " + this.value);

        if (this.leftChild != null) {
            System.out.println("leftchild: ");
            this.leftChild.showTree();
        }
        if (this.rightChild != null) {
            System.out.println("rightchild: ");
            this.rightChild.showTree();
        }
    }


    void G_test1() {
        GVariable X = new GVariable("x");
        GVariable Y = new GVariable("y");
        GConstant two = new GConstant("2");
        GOperation Z = new GOperation("mult", X, two);
        GOperation E = new GOperation("cos", Z, null);

        System.out.println(E.expression());

        GOperation DE = E.particalGradient(X);
        System.out.println(DE.expression());
    }

    void E_test1() {
        ExpressionTree e = new ExpressionTree("mult(add(2,x),cos(x))");
        System.out.println(String.valueOf(e.evaluate(2)));

        GBaseClass ast = e.ast;
        GVariable X = new GVariable("x");
        GOperation DX = (GOperation) ast.particalGradient(X);
        System.out.println(DX.expression());
    }

    void E_test2() {
        ExpressionTree e = new ExpressionTree("exp(mult(add(2,x),cos(x)))");
        ExpressionTree de = e.differentiate();

        System.out.println(e);
        System.out.println(e.evaluate(2));
        System.out.println("de: " + de);
        System.out.println(de.evaluate(2));
    }

    public static void main(String args[]) {
        ExpressionTree e = new ExpressionTree();
        e.E_test2();
    }
}
