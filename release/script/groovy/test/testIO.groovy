package test

import com.guan.code.UT
import com.guan.math.MathEX
import com.guan.plot.Plotters


gr = UT.IO.csv2data('1253/.temp/rdf.csv'); // 可能因为 err 输出管线堵塞导致死锁

plt = Plotters.get();
plt.plot(MathEX.Mat.getColumn(gr, 1), MathEX.Mat.getColumn(gr, 0), 'RDF');
plt.show();
