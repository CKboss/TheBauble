function [ flag , pid ] = checkKind( x , kind)
%CHECK Summary of this function goes here
%   Detailed explanation goes here
    flag = true;
    pid = -1;
    if kind==1
        base = 0;
    else
        base = 20;
    end
    for i = 1:length(x)
        id = i + base;
        if getKindByAlpha(id) ~= kind
            flag = false;
            pid = i;
            break;
        end
    end
end

