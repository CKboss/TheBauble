function [ w, b, a ] = svm_d( X , Y )
%SVM_D Summary of this function goes here
%   Detailed explanation goes here
    [n,m] = size(X);
    
    Q = zeros(n,n);
    for i = 1:n
        for j = 1:n
            Q(i,j) = X(i,:)*X(j,:)';
        end
    end

    cvx_begin
        variable a(n);
        minimize(0.5*quad_form(Y.*a,Q)-sum(a))
        subject to
            Y'*a==0;
            a>=0;
    cvx_end
    
    w = (a.*Y)'*X;
    w = w';
    id = find(a>1e-3,1);
    b = Y(id) - (a.*Y)'*(X*X(id,:)');
end

