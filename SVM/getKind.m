% 判别函数
function kind = getKind(w,b,x)
    tmp = x*w' + b;
    if tmp >= 10^-2
        kind = 1;
    elseif tmp <= -10^-2
        kind = -1;
    else
        kind = -2;
    end
end