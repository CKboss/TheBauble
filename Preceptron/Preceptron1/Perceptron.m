% 假设的分界面
W_ = [50 , -345 ];
b_ = 27.4;

[p1, p2] = getInitvalue(W_,b_);

w = 0;
b = 0;

loop = 100000;

scalar = 0.1;

while loop >=0
    loop=loop-1;
    % check error
    % check 1
    [ok,id] = checkKind(w,b,p1,1);
    if ok == true
        % check -1
        [ok , id] = checkKind(w,b,p2,-1);
        if ok==true
            disp('find answer')
            break
        else
            w = w + scalar*-1*p2(id,:);
            b = b + scalar*-1;
        end
    else
        w = w + scalar*p1(id,:);
        b = b + scalar;
    end
end

%ShowGraph;
