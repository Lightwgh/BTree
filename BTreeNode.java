package btree;

import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BTreeNode {
    int M;
    BTreeNode parent;
    LinkedList<Integer> values;
    LinkedList<BTreeNode> children;
    private BTreeNode() {
        values = new LinkedList<>();
        children = new LinkedList<>();
    }
    public BTreeNode(int M) {
        this();
        this.M = M;
    }
    public BTreeNode(BTreeNode parent) {
        this(parent.M);
        this.parent = parent;
    }
    public boolean isEmpty() {
        if (this.values == null || this.values.size() == 0) {
            return true;
        }
        return false;
    }
    public boolean isRoot() {
        if (this.parent == null) {
            return true;
        }
        return false;
    }
    public BTreeNode getRoot() {
        BTreeNode p = this;
        while (!p.isRoot()) {
            p = p.parent;
        }
        return p;
    }
    public BTreeNode insert(int e) {
        if (isEmpty()) {
            values.add(e);
            children.add(new BTreeNode(this));
            children.add(new BTreeNode(this));
            return this;
        }
        BTreeNode node = search(e);
        if (!node.isEmpty()) {
            throw new RuntimeException("插入了一个同值数据");
        }
        insert(node.parent, e, new BTreeNode(M));

        return getRoot();
    }
    public void insert(BTreeNode node, int e, BTreeNode rChildrenNode) {
        int valueIndex = 0;
        while (valueIndex < node.values.size() && node.values.get(valueIndex) < e) {
            valueIndex++;
        }
        node.values.add(valueIndex, e);
        node.children.add(valueIndex + 1, rChildrenNode);
        node.children.get(valueIndex + 1).parent = node;
        if (node.values.size() == M) {
            int upIndex = M / 2;
            int upValue = node.values.get(M / 2);
            BTreeNode rNode = new BTreeNode(M);
            rNode.values = new LinkedList<>(node.values.subList(upIndex + 1, M));
            rNode.children = new LinkedList<>(node.children.subList(upIndex + 1, M + 1));
            for (BTreeNode rChild: rNode.children) {
                rChild.parent = rNode;
            }

            node.values = new LinkedList<>(node.values.subList(0, upIndex));
            node.children = new LinkedList<>(node.children.subList(0, upIndex + 1));
            for (BTreeNode rChild: node.children) {
                rChild.parent = node;
            }
            if (node.parent == null) {
                BTreeNode parent = new BTreeNode(M);
                parent.values.add(upValue);
                parent.children.add(node);
                parent.children.add(rNode);
                node.parent = parent;
                rNode.parent = parent;
                return;
            }
            insert(node.parent, upValue, rNode);
        }
    }
    public BTreeNode search(int target) {
        if (isEmpty()) {
            return this;
        }
        BTreeNode root = getRoot();
//        System.out.println("round ==> " +  values.size());
//        for (int ele: root.values) {
//            System.out.print(ele + ", ");
//        }
//        System.out.println();
        int left = 0, right = values.size();
       // System.out.println(left + ", " + right);
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (values.get(mid) == target) {
                return this;
            } else if (values.get(mid) < target) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
       // System.out.println("search ==> " + left);
        return children.get(left).search(target);
    }
    public List<List<String>> print() {
        BTreeNode root = getRoot();
        List<List<String>> ans = new ArrayList<>();
        String s = convertBTreeNodeToString(this);
        ArrayList<String> rootL = new ArrayList<>();
        rootL.add(s);
        ans.add(new ArrayList<>(rootL));

        Queue<BTreeNode> queue = new LinkedList<>();
        queue.offer(this);
        while (!queue.isEmpty()) {
            int size = queue.size();
            List<String> l = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                BTreeNode node = queue.poll();
                for (BTreeNode child: node.children) {
                    queue.offer(child);
                    l.add(convertBTreeNodeToString(child));
                }
            }
            if (l.size() != 0)
                ans.add(new ArrayList<>(l));
        }
        System.out.println(ans.toString());
        return ans;
    }
    public BTreeNode delete(int e) {
        if (isEmpty()) {
            return this;
        }
        BTreeNode p = getRoot().search(e);
        if (p.isEmpty()) {
            throw new RuntimeException("the key to be deleted is not exist");
        }
        int valueIndex = 0;
        while (valueIndex < p.values.size() && p.values.get(valueIndex) < e) {
            valueIndex++;
        }
        if (!p.children.get(0).isEmpty()) {
            BTreeNode rMin = p.children.get(valueIndex + 1);
            while (!rMin.children.get(0).isEmpty()) {
                rMin = rMin.children.get(0);
            }
            p.values.set(valueIndex, rMin.values.get(0));
            return delete(rMin, 0, 0);
        }
        return delete(p, valueIndex, 0);
    }
    public BTreeNode delete(BTreeNode target, int valueIndex, int childIndex) {
        target.values.remove(valueIndex);
        target.children.remove(childIndex);
        if (target.values.size() >= ((M + 1) / 2) - 1) {
            return target.getRoot();
        }
        if (target.isRoot()) {
            if (target.values.size() >= 1) {
                return target;
            }
            target.children.get(0).parent = null;
            return target.children.get(0);
        }
        int parentChildIndex = 0;
        BTreeNode targetParent = target.parent;
        while (parentChildIndex < targetParent.children.size() && targetParent.children.get(parentChildIndex) != target) {
            parentChildIndex++;
        }
        if (parentChildIndex > 0 && targetParent.children.get(parentChildIndex - 1).values.size() > ((M + 1) / 2) - 1) {
            BTreeNode leftSibling = targetParent.children.get(parentChildIndex - 1);
            int downKey = targetParent.values.get(parentChildIndex - 1);
            int upKey = leftSibling.values.get(leftSibling.values.size() - 1);
            leftSibling.values.remove(leftSibling.values.size() - 1);
            BTreeNode mergeChild = leftSibling.children.remove(leftSibling.values.size() - 1);
            targetParent.values.set(parentChildIndex - 1, upKey);
            target.values.add(0, downKey);
            target.children.add(0, mergeChild);
            return target.getRoot();
        } else if (parentChildIndex < targetParent.children.size() - 1 && targetParent.children.get(parentChildIndex + 1).values.size() > ((M + 1) / 2) - 1) {
            BTreeNode rightSibling = targetParent.children.get(parentChildIndex + 1);
            int downKey = targetParent.values.get(parentChildIndex);
            int upKey = rightSibling.values.get(0);
            rightSibling.values.remove(0);
            BTreeNode mergChild = rightSibling.children.remove(0);
            targetParent.values.set(parentChildIndex, upKey);
            target.values.add(downKey);
            target.children.add(mergChild);
            return target.getRoot();
        } else {
            int parentValueIndex = 0;
            if (parentChildIndex > 0) {
                parentValueIndex = parentChildIndex - 1;
                BTreeNode leftSibling = targetParent.children.get(parentChildIndex - 1);
                int downKey = targetParent.values.get(parentValueIndex);
                leftSibling.values.add(downKey);
                leftSibling.values.addAll(target.values);
                target.children.forEach(c -> c.parent = leftSibling);
                leftSibling.children.addAll(target.children);
            } else {
                parentValueIndex = parentChildIndex;
                BTreeNode rightSibling = targetParent.children.get(parentChildIndex + 1);
                int downKey = targetParent.values.get(parentValueIndex);
                rightSibling.values.add(0, downKey);
                rightSibling.values.addAll(0, target.values);
                target.children.forEach(c -> c.parent = rightSibling);
                rightSibling.children.addAll(0, target.children);
            }
            return delete(targetParent, parentValueIndex, parentChildIndex);
        }
    }
    public String convertBTreeNodeToString(BTreeNode node) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        if (node.values.size() == 0) {
            return "()";
        }
        for (int val: node.values) {
            sb.append(val + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }
    public static void main(String[] args) {
//        BTreeNode node = new BTreeNode(3);
//        node = node.insert(35);
//        node = node.insert(13);
//        node = node.insert(17);
//        node = node.insert(2);
//        node.print();
//        node = node.delete(2);
//        node.print();


//        BTreeNode node = new BTreeNode(3);
//        node = node.insert(35);
//        node = node.insert(13);
//        node = node.insert(17);
//        node = node.insert(2);
//        node.print();
//        node = node.delete(35);
//        node.print();

//        BTreeNode node = new BTreeNode(3);
//        node = node.insert(35);
//        node = node.insert(13);
//        node = node.insert(17);
//        node = node.insert(22);
//        node.print();
//        node = node.delete(13);
//        node.print();

        BTreeNode node = new BTreeNode(3);
        node = node.insert(2);
        node = node.insert(13);
        node = node.insert(15);
        node = node.insert(25);
        node = node.insert(23);
        node = node.insert(35);
        node = node.insert(24);
        node = node.insert(4);
        node = node.insert(17);
        node.print();
        System.out.println("-----------------");
        node = node.delete(4);
        node.print();
        System.out.println("-----------------");
        node = node.delete(17);
        node.print();
        System.out.println("-----------------");
        node = node.delete(15);
        node.print();
        System.out.println("-----------------");
        node = node.delete(24);
        node.print();
        System.out.println("-----------------");
        node = node.delete(23);
        node.print();
        System.out.println("-----------------");
        node = node.delete(35);
        node.print();
        System.out.println("-----------------");

//        for (int val: root.parent.values) {
//            System.out.println(val);
//        }
    }
}
