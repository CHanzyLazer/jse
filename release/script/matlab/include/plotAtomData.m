function fig = plotAtomData(jAtomData, colors, sizes, extend)
%绘制 atomData
% extend 可选，x y z 三个方向负正的延申次数
% box 可选，x y z 三个方向的延申长度

fig = figure;

orthogonalXYZ = jAtomData.orthogonalXYZ;
typeCol = jAtomData.typeCol+1;
if typeCol > 0
    atomData = jAtomData.atomData;
    atomTypes = atomData(:, typeCol);
else
    atomTypes = ones(jAtomData.atomNum, 1);
end

scatter3(orthogonalXYZ(:, 1), orthogonalXYZ(:, 2), orthogonalXYZ(:, 3), sizes(atomTypes), colors(atomTypes, :), "filled");

hold on
grid on
if nargin > 3
    box = jAtomData.boxHi-jAtomData.boxLo;
    for i = extend(1,1):extend(1,2)
    for j = extend(2,1):extend(2,2)
    for k = extend(3,1):extend(3,2)
    if ~(i == 0 && j == 0 && k == 0)
        scatter3(orthogonalXYZ(:, 1)+box(1)*i, orthogonalXYZ(:, 2)+box(2)*j, orthogonalXYZ(:, 3)+box(3)*k, sizes(atomTypes), colors(atomTypes, :), "filled");
    end
    end
    end
    end
end

axis equal

end
