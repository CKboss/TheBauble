figure(1);

% points kind = 1
scatter(p1(:,1),p1(:,2),'.','r');

hold on;

% points kind = -1
scatter(p2(:,1),p2(:,2),'.','b');

for i = 1:length(p1)
    if abs(p1(i,1:2)*w+b-1) <= 1e-4
        scatter(p1(i,1),p1(i,2),'o','r');
    end
end

for i = 1:length(p2)
    if abs(p2(i,1:2)*w+b+1) <= 1e-4
        scatter(p2(i,1),p2(i,2),'o','b');
    end
end

hold on;

% 分离平面
line_x = linspace(min(min(p1(:)),min(p2(:))),max(max(p1(:)),max(p2(:))),30);
line_y = -(w(1)*line_x+b)/w(2);
plot(line_x,line_y);