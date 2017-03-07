
[p1,p2] = getInitvalue(-300,30,30,40);
P = [ [p1,ones(size(p1,1),1)]; [p2,ones(size(p2,1),1).*-1] ];

X = P(:,1:2);
Y = P(:,3);

[n,m] = size(X);


[w,b,a] = svm_d(X,Y);

ShowGraph;

% 
% Q = zeros(n,n);
% for i = 1:n
%     for j = 1:n
%         Q(i,j) = X(i,:)*X(j,:)';
%     end
% end
% 
% cvx_begin
%     variable a(n);
%     minimize(0.5*quad_form(Y.*a,Q)-sum(a))
%     subject to
%         Y'*a==0;
%         a>=0;
%         a<=100;
% cvx_end