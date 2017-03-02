% 判别函数
function kind = getKindByAlpha(id)
    global Gram;
    global alpha b;
    global P;
    tmp = 0;
    for j = 1:length(P)
        % tmp = tmp + alpha(j)*P(j,3)*P(j,1:2)*P(id,1:2)';
        tmp = tmp + alpha(j)*P(j,3)*Gram(j,id);
    end
    tmp = tmp + b;
    if tmp >= 10^-6
        kind = 1;
    else
        kind = -1;
    end
end