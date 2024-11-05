
clear all, close all;

global pos1 vec1 pos2 vec2 steps;

pos1 = [10 0];  % startposition
vec1 = [-1 0];  % startvektor
pos2 = [0 10];  % endposition
vec2 = [1 0];   % endvektor
n = 10;         % punkte

vec1 = vec1/norm(vec1);
vec2 = vec2/norm(vec2);
steps = 0:1/n:1; 

plotcurve();

function plotcurve()
    global pos1 vec1 pos2 vec2 steps
    
    [X,Y] = curve(pos1,vec1,pos2,vec2,steps);
    plot(X,Y,"*-");
end

function [X,Y]=curve(p1,v1,p2,v2,inter)
    dist = sqrt((p1(1) - p2(1)).^2 + (p1(2) - p2(2)).^2);
    p12 = p1 + v1 * dist / 2;
    p22 = p2 + v2 * dist / 2;

    for i=1:1:length(inter)
        [X(i),Y(i)] = bezier([p1(1), p12(1), p22(1), p2(1)], [p1(2), p12(2), p22(2), p2(2)], inter(i));
    end
end

function [X,Y]=bezier(pX,pY,inter)
    for l=length(pX)-1:-1:0
        for i=1:+1:l
            pX(i) = (pX(i + 1) - pX(i)) * inter + pX(i);
            pY(i) = (pY(i + 1) - pY(i)) * inter + pY(i);
        end
    end
    
    X = pX(1);
    Y = pY(1);
end
