% 判别函数
function kind = getKind(w,b,x)
    tmp = x*w' + b;
    if tmp >= 10^-6
        kind = 1;
    else
        kind = -1;
    end
end