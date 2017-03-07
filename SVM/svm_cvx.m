function [w,b] = svm_cvx(P)
    [n,m] = size(P);
    X = P(:,1:2);
    Y = P(:,3);
    cvx_begin
        variable b;
        variable w(m-1);
        minimize(norm(w,2));
        subject to
            for i = 1:n
                Y(i)*(X(i,:)*w+b) -1 >= 0;
            end
    cvx_end
end