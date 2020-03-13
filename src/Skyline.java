package YuangLiu.JunXiao.CS562Project;

import com.github.davidmoten.rtree2.*;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.HasGeometry;
import com.github.davidmoten.rtree2.geometry.Point;

import java.io.*;
import java.util.*;

public class Skyline {
    private RTree<Integer, Point> rtree;
    private List<Entry<Integer, Point>> skyline;

    public Skyline(String dataPath) throws IOException {
        skyline = new ArrayList<>();
        rtree = RTree.maxChildren(8).create();
        List<String> data = readData(dataPath);
        buildRtree(data);
        skylineAlg();
        skyline.sort(Comparator.comparingDouble(o -> o.geometry().x()));
        output();
    }

    private void output() {
        System.out.println("The skyline is:");
        for (Entry<Integer, Point> entry : skyline) {
            System.out.println(entry.geometry().x() + " " + entry.geometry().y());
        }
        System.out.println();
    }

    public void delete(double x, double y) {
        System.out.println("Delete: (" + x + ", " + y + ") from rtree.");
        Iterable<Entry<Integer, Point>> cur = rtree.search(Geometries.point(x, y));
        rtree = rtree.delete(cur);
        for (Entry<Integer, Point> entry : cur) {
            if (skyline.contains(entry)) {
                System.out.println("Delete: (" + entry.geometry().x() + " " + entry.geometry().y() + ") from skyline.");
                skyline.remove(entry);
                skylineAlg();
                skyline.sort(Comparator.comparingDouble(o -> o.geometry().x()));
            }
        }
        output();
    }

    public void insert(double x, double y) {
        System.out.println("Insert: (" + x + ", " + y + ") into rtree.");
        Iterable<Entry<Integer, Point>> all = rtree.entries();
        int max = 0;
        for (Entry<Integer, Point> entry : all)
            if (entry.value() > max)
                max = entry.value();
        Entry<Integer, Point> cur = rtree.context().factory().createEntry(max + 1, Geometries.point(x, y));
        rtree = rtree.add(cur);
        if (notDominate(cur)) {
            System.out.println("Insert: (" + x + ", " + y + ") into skyline.");
            List<Entry<Integer, Point>> kill = new ArrayList<>();
            for (Entry<Integer, Point> c : skyline) {
                if (c.geometry().x() > x && c.geometry().y() > y)
                    kill.add(c);
                else if (c.geometry().x() == x && c.geometry().y() > y)
                    kill.add(c);
                else if (c.geometry().x() > x && c.geometry().y() == y)
                    kill.add(c);
            }
            if (kill.size() > 0) {
                System.out.println("Delete these point(s) from skyline:");
                for (Entry<Integer, Point> c : kill)
                    System.out.println("(" + c.geometry().x() + ", " + c.geometry().y() + ")");
            }
            skyline.removeAll(kill);
            skyline.add(cur);
        }
        skyline.sort(Comparator.comparingDouble(o -> o.geometry().x()));
        output();
    }

    private static double getMinDist(HasGeometry e) {
        double dist;
        if (e instanceof NonLeaf) {
            NonLeaf<Integer, Point> n = (NonLeaf<Integer, Point>) e;
            dist = n.geometry().mbr().x1() + n.geometry().mbr().y1();
        }
        else {
            if (e instanceof Leaf) {
                Leaf<Integer, Point> n = (Leaf<Integer, Point>) e;
                dist = n.geometry().mbr().x1() + n.geometry().mbr().y1();
            }
            else {
                Entry<Integer, Point> entry = (Entry<Integer, Point>) e;
                dist = entry.geometry().x() + entry.geometry().y();
            }
        }
        return dist;
    }

    private void skylineAlg() {
        PriorityQueue<HasGeometry> heap = new PriorityQueue<>(Comparator.comparingDouble(Skyline::getMinDist));
        Node<Integer, Point> root;
        if (rtree.root().isPresent())
            root = rtree.root().get();
        else
            return;
        heap.add(root);
        while(!heap.isEmpty()) {
            HasGeometry cur = heap.poll();
            if (notDominate(cur)) {
                if (cur instanceof NonLeaf) {
                    NonLeaf<Integer, Point> n = (NonLeaf<Integer, Point>) cur;
                    for (int i = 0; i < n.count(); i++) {
                        if (notDominate(n.child(i)))
                            heap.add(n.child(i));
                    }
                }
                else {
                    if (cur instanceof Leaf) {
                        Leaf<Integer, Point> n = (Leaf<Integer, Point>) cur;
                        for (Entry<Integer, Point> entry : n.entries()) {
                            if (notDominate(entry))
                                heap.add(entry);
                        }
                    }
                    else {
                        Entry<Integer, Point> entry = (Entry<Integer, Point>) cur;
                        if (!skyline.contains(entry))
                            skyline.add(entry);
                    }
                }
            }
        }
    }

    private boolean notDominate(HasGeometry cur) {
        for (Entry<Integer, Point> e : skyline) {
            if (cur instanceof NonLeaf) {
                NonLeaf<Integer, Point> n = (NonLeaf<Integer, Point>) cur;
                if ((n.geometry().mbr().x1() > e.geometry().x() && n.geometry().mbr().y1() >= e.geometry().y()) ||
                        (n.geometry().mbr().x1() >= e.geometry().x() && n.geometry().mbr().y1() > e.geometry().y()))
                    return false;
            }
            else {
                if (cur instanceof Leaf) {
                    Leaf<Integer, Point> n = (Leaf<Integer, Point>) cur;
                    if ((n.geometry().mbr().x1() > e.geometry().x() && n.geometry().mbr().y1() >= e.geometry().y()) ||
                            (n.geometry().mbr().x1() >= e.geometry().x() && n.geometry().mbr().y1() > e.geometry().y()))
                        return false;
                }
                else {
                    Entry<Integer, Point> entry = (Entry<Integer, Point>) cur;
                    if ((entry.geometry().x() > e.geometry().x() && entry.geometry().y() >= e.geometry().y()) ||
                            (entry.geometry().x() >= e.geometry().x() && entry.geometry().y() > e.geometry().y()))
                        return false;
                }
            }
        }
        return true;
    }

    private void DFSTraverse() {
        Queue<HasGeometry> queue = new LinkedList<>();
        Node<Integer, Point> root;
        if (rtree.root().isPresent())
             root = rtree.root().get();
        else
            return;
        queue.add(root);
        int count = 0;
        while(!queue.isEmpty()) {
            HasGeometry cur = queue.poll();
            if (cur instanceof NonLeaf) {
                NonLeaf<Integer, Point> n = (NonLeaf<Integer, Point>) cur;
                for (int i = 0; i < n.count(); i++)
                    queue.add(n.child(i));
            }
            else {
                if (cur instanceof Leaf) {
                    Leaf<Integer, Point> n = (Leaf<Integer, Point>) cur;
                    queue.addAll(n.entries());
                }
                else {
                    Entry<Integer, Point> entry = (Entry<Integer, Point>) cur;
                    System.out.println(entry);
                    count += 1;
                }
            }
        }
        System.out.println(count);
    }

    private void buildRtree(List<String> data) {
        for (int i = 1; i <= data.size(); i++) {
            String str = data.get(i - 1);
            String[] points = str.split(" ");
            if (points.length > 1) {
                double x = Double.parseDouble(points[0]);
                double y = Double.parseDouble(points[1]);
                rtree = rtree.add(i, Geometries.point(x, y));
            }
        }
    }

    private List<String> readData(String dataPath) throws IOException {
        File file = new File(dataPath);
        InputStreamReader read = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt;
        List<String> list = new ArrayList<>();

        while ((lineTxt = bufferedReader.readLine()) != null)
            list.add(lineTxt);
        bufferedReader.close();
        read.close();
        return list;
    }
}
