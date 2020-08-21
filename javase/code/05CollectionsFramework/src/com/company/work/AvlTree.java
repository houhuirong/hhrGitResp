package com.company.work;

import java.util.Comparator;

public class AvlTree<AnyType extends Comparable<? super AnyType>> {

    private AvlNode<AnyType> root;
    private Comparator<? super AnyType> cmp;

    private static class AvlNode<AnyType> {
        AnyType element;
        AvlNode<AnyType> left;
        AvlNode<AnyType> right;
        int height;

        AvlNode(AnyType theElement) {
            this(theElement, null, null);
        }

        AvlNode(AnyType theElement, AvlNode<AnyType> lt, AvlNode<AnyType> rt) {
            element = theElement;
            left = lt;
            right = rt;
            height = 0;
        }
    }

    private AvlNode leftLeftRotation(AvlNode avlNode) {
        AvlNode left = avlNode.left;
        avlNode.left = left.right;
        left.right = avlNode;

        avlNode.height = Integer.max(height(avlNode.left), height(avlNode.right)) + 1;
        left.height = Integer.max(height(left.left), avlNode.height) + 1;  //avlNode�ĸ߶ȣ�����left�����������ĸ߶�
        return left;
    }

    private AvlNode rightRightRotation(AvlNode avlNode) {
        AvlNode right = avlNode.right;
        avlNode.right = right.left;
        right.left = avlNode;

        avlNode.height = Integer.max(height(avlNode.left), height(avlNode.right)) + 1;
        right.height = Integer.max(height(right.right), avlNode.height) + 1;
        return right;
    }

    private AvlNode leftRightRotation(AvlNode avlNode) { //lr�����������ת��������������ת��
        avlNode.left = rightRightRotation(avlNode.left); 
        return leftLeftRotation(avlNode);
    }

    private AvlNode rightLeftRotation(AvlNode avlNode) {//rl�����������ת��������������ת��
        avlNode.right = leftLeftRotation(avlNode.right);
        return rightRightRotation(avlNode);
    }

    private AvlNode insert(AvlNode<AnyType> tree, AnyType key) {
        if (tree == null) {
            tree = new AvlNode(key, null, null);
        } else {
            int cmp = key.compareTo(tree.element);
            if (cmp > 0) { //���ұ�
                tree.right = insert(tree.right, key);
                if (height(tree.right) - height(tree.left) == 2) {
                    if (key.compareTo(tree.right.element) > 0)
                        tree = rightRightRotation(tree); //���key�����������ұߣ�����rr���
                    else
                        tree = rightLeftRotation(tree); //���key������������ߣ�����rl�����
                }


            } else if (cmp < 0) {//�����
                tree.left = insert(tree.left, key);
                if (height(tree.left) - height(tree.right) == 2) {
                    if (key.compareTo(tree.left.element) < 0)
                        tree = leftLeftRotation(tree);//������������ߣ�ll���
                    else
                        tree = leftRightRotation(tree);//�����������ұߣ�lr���
                }
            } else {
                System.out.println("���ʧ�ܣ������������ͬ�Ľڵ㣡");
            }

        }

        tree.height = Integer.max(height(tree.left), height(tree.right)) + 1;
        return tree;
    }

    public void insert(AnyType anyType){
      this.root= insert(this.root,anyType);
        this.inOrder();
        System.out.println("==============");
    }

    private void inOrder(AvlNode<AnyType> tree) {
        if (tree != null) {

            inOrder(tree.left);
            for(int i=0;i<tree.height;i++){
                System.out.print(" ");
            }
            System.out.println(tree.element + ":"+tree.height+" ");
            inOrder(tree.right);
        }
    }

    public void inOrder() {
        inOrder(this.root);
    }

    private int height(AvlNode<AnyType> tree) {
        if (tree != null)
            return tree.height;

        return -1;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        AvlTree<Integer> avlTree = new AvlTree<>();
        avlTree.insert(1);
        avlTree.insert(2);
        avlTree.insert(3);
        avlTree.insert(4);
        avlTree.insert(5);
        avlTree.insert(6);
        avlTree.insert(7);
        avlTree.insert(8);
        avlTree.insert(9);
        avlTree.insert(10);
        System.out.println(System.currentTimeMillis()-start);

    }

}