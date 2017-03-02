% 假设的分界面
W_ = [50 , -345 ];
b_ = 27.4;

[p1, p2] = getInitvalue(W_,b_);

%p1 = [ 3 3 ; 4 3 ];
%p2 = [ 1 1 ];
global P;
P = [[p1;p2] [ones(size(p1,1),1);ones(size(p2,1),1)*-1]];

global Gram;
Gram = P(:,1:end-1)*P(:,1:end-1)';

global alpha b;
alpha = zeros(length(P),1);
b = 0;
scalar = 0.1;

loop = 100000;
base = 20;

while loop >=0
    loop=loop-1;
    % check error
    % check 1
    [ok,id] = checkKind(p1,1);
    %fprintf('kind 1 : %d,%d\n',ok,id);
    if ok == true
        % check -1
        [ok , id] = checkKind(p2,-1);
        %fprintf('kind -1 : %d,%d\n',ok,id);
        if ok==true
            disp('find answer')
            break
        else
            alpha(id+base) = alpha(id+base) + scalar;
            b = b + scalar * -1;
        end
    else
         alpha(id) = alpha(id) + scalar;
         b = b + scalar;
    end
end

ShowGraph;
