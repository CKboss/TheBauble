figure(1);

% points kind = 1
scatter(p1(:,1),p1(:,2),'o','r');

hold on;

% points kind = -1
scatter(p2(:,1),p2(:,2),'o','b');


hold on;

w = [0 0];
b = 0;

for i = 1:length(P)
    w = w + alpha(i)*P(i,3)*P(i,1:2);
    b = b + alpha(i)*P(i,3);
end

% 分离平面
line_x = linspace(min(min(p1(:)),min(p2(:))),max(max(p1(:)),max(p2(:))),30);
line_y = -(w(1)*line_x+b)/w(2);


plot(line_x,line_y);