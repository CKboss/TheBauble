% INIT
function [p1 , p2] = getInitvalue(w,b,na,nb,dim)
    if nargin == 2
        na = 20;
        nb = 20;
        dim = 2;
    end
    if nargin == 4
        dim = 2;
    end

    p1 = zeros(na,dim);
    p2 = zeros(nb,dim);
    n1 = 1;
    n2 = 1;
    while n1<=na || n2<=nb
        tmp = random('normal',10,20,1,dim);
        kind = getKind(w,b,tmp);
        if kind==-2
            continue
        end
        if kind==1 && n1<=na
            p1(n1,:) = tmp;
            n1 = n1 + 1;
        elseif kind==-1 && n2<=nb
            p2(n2,:) = tmp;
            n2 = n2 + 1;
        end
    end
end