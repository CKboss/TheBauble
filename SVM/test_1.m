
% get Data
[p1,p2] = getInitvalue(-300,30,30,40);
P = [ [p1,ones(size(p1,1),1)]; [p2,ones(size(p2,1),1).*-1] ];

% use svm_cvx to get w and b

[w,b] = svm_cvx(P);

ShowGraph;