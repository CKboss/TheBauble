% INIT
function [p1 , p2] = getInitvalue(w,b,dim)
    if nargin == 2
        dim = 2;
    end
    p1 = zeros(20,dim);
    p2 = zeros(20,dim);
    n1 = 1;
    n2 = 1;
    while n1<=20 || n2<=20
        tmp = random('normal',10,20,1,dim);
        kind = getKind(w,b,tmp);
        if kind==-2
            continue
        end
        if kind==1 && n1<=20
            p1(n1,:) = tmp;
            n1 = n1 + 1;
        elseif kind==-1 && n2<=20
            p2(n2,:) = tmp;
            n2 = n2 + 1;
        end
    end
end