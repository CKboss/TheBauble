function [ flag , pid ] = checkKind( w , b , x , kind)
%CHECK Summary of this function goes here
%   Detailed explanation goes here
    flag = true;
    pid = -1;
    for i = 1:length(x)
        if getKind(w,b,x(i,:)) ~= kind
            flag = false;
            pid = i;
            break;
        end
    end
end

