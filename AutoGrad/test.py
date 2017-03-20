class GBaseClass(object):
    def __init__(self,name,value,type_):
        self.name = name
        self.value = value
        self.type = type_
    def partialGradient(self,partical):
        pass
    def expression(self):
        pass
    
G_STANTIC_VARIABLE = dict()

class GConstant(GBaseClass):
    def __init__(self,value):
        global G_STANTIC_VARIABLE
        try:
            G_STANTIC_VARIABLE['counter'] += 1
        except:
            G_STANTIC_VARIABLE['counter'] = 0    
        self.value = value
        self.name = 'CONSTANT_'+str(G_STANTIC_VARIABLE['counter'])
        self.type = 'CONSTANT'
    def partialGradient(self,partical):
        return GConstant(0)
    def expression(self):
        return str(self.value)
    
class GVariable(GBaseClass):
    def __init__(self, name, value=None):
        self.name = name
        self.value = value
        self.type = 'VARIABLE'

    def partialGradient(self, partial):
        if partial.name == self.name:
            return GConstant(1)
        return GConstant(0)

    def expression(self):
        return str(self.name)
    
class GOperation(GBaseClass):
    def __init__(self,a,b,operType):
        self.operatorType = operType
        self.left = a
        self.right = b
    def partialGradient(self,partial):
        if partial.type != 'VARIABLE':
            print('not variable')
            return None
        if self.operatorType == 'plus' or self.operatorType == 'minus':
            return GOperationWrapper(self.left.partialGradient(partial),
                                     self.right.partialGradient(partial),
                                     self.operatorType)
        if self.operatorType == 'multiple':
            print('in multiple')
            part1 = GOperationWrapper(self.left.partialGradient(partial),self.right,'multiple')
            part2 = GOperationWrapper(self.left,self.right.partialGradient(partial),'multiple')
            return GOperationWrapper(part1,part2,'plus')
        if self.operatorType == 'division':
            part1 = GOperationWrapper(self.left.partialGradient(partial), self.right, "multiple")
            part2 = GOperationWrapper(self.left, self.right.partialGradient(partial), "multiple")
            part3 = GOperationWrapper(part1, part2, "minus")
            part4 = GOperationWrapper(self.right,GConstant(2),'pow')
            part5 = GOperationWrapper(part3,part4,'division')
            return part5
        if self.operatorType == 'pow':
            c = GConstant(self.right.value-1)
            part2 = GOperationWrapper(self.left,c,'pow')
            part3 = GOperationWrapper(self.right,part2,'multiple')
            return GOperationWrapper(self.left.partialGradient(partial),part3,'multiple')
        if self.operatorType == "exp":
            return GOperationWrapper(self.left.partialGradient(partial), self, "multiple")
        if self.operatorType == "ln":
            part1 = GOperationWrapper(GConstant(1),self.left,"division")
            rst = GOperationWrapper(self.left.partialGradient(partial), part1, "multiple")
            return rst
        return None
    def expression(self):
        if self.operatorType == "plus":
            return self.left.expression() + "+" + self.right.expression()

        if self.operatorType == "minus":
            return self.left.expression() + "-" + self.right.expression()

        if self.operatorType == "multiple":
            return "(" + self.left.expression() + ")*(" + self.right.expression() + ")"

        if self.operatorType == "division":
            return "(" + self.left.expression() + ")/(" + self.right.expression() + ")"

        # pow should be x^a,a is a constant.
        if self.operatorType == "pow":
            return "(" + self.left.expression() + ")**(" + self.right.expression() + ")"

        if self.operatorType == "exp":
            return "exp(" + self.left.expression() + ")"

        if self.operatorType == "ln":
            return "ln(" + self.left.expression() + ")"

    def type(self):
        return "OPERATION"
        
def GOperationWrapper(left,right,operType):
     if operType == "multiple":
        if left.type == "CONSTANT" and right.type == "CONSTANT":
            return GConstant(left.value * right.value)
        if left.type == "CONSTANT" and left.value == 1:
            return right
        if left.type == "CONSTANT" and left.value == 0:
            return GConstant(0)
        if right.type == "CONSTANT" and right.value == 1:
            return left
        if right.type == "CONSTANT" and right.value == 0:
            return GConstant(0)
     if operType == "plus":
        if left.type == "CONSTANT" and left.value == 0:
            return right
        if right.type == "CONSTANT" and right.value == 0:
            return left
     if operType == "minus":
        if right.type == "CONSTANT" and right.value == 0:
            return left
       # if right.type == 'CONSTANT' and left.type == 'CONSTANT':
       #     return right.value-left.value
     return GOperation(left, right, operType)
 
def exp(x):
    import math
    return math.exp(x)

def run(express,valuedict):
    for key,value in valuedict.items():
        txt = str(key)+' = '+str(value)
        print('exec: %s'%txt)
        exec(txt)
    print('eval: %s'%express)
    return eval(express)
        
def sigmoid(X):
    a = GConstant(1.0)
    b = GOperationWrapper(GConstant(0), X, 'minus')
    c = GOperationWrapper(b, None, 'exp')
    d = GOperationWrapper(a, c, 'plus')
    rst = GOperationWrapper(a, d, 'division')
    return rst       

def F1(X):
    a = GConstant(30)
    b = GOperation(a,X,'multiple')
    return b


x = GVariable('x')
s = sigmoid(x)
print(s.expression())
print(s.partialGradient(x).expression())
print(run(s.partialGradient(x).expression(),{'x':10}))

'''
if __name__=='__main__':
    x = GVariable('x')
    y = GVariable('y')
    x_add_y = GOperation(x,y,'plus')
    dxy = x_add_y.particalGradint(x)
    print(x_add_y.expression())
    print(dxy.expression())
'''        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        #