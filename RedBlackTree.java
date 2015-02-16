package red_black_tree;
/**
 * Class implementing a red-black tree.
 * <p>
 * Note that all matching/comparison is based on the Comparable interface (not equals()). No repeated elements are allowed.
 * </p>
 * <p>
 * This code is extended from that provided by Mark Allen Weiss 
 * at http://www.java-tips.org/java-se-tips/java.lang/red-black-tree-implementation-in-java.html and Wikipedia (particularly the removal operations)
 * </p>
 * 
 * <p>
 * Invariants are as follows:
 * 		i) 		All nodes are BLACK or RED
 * 		ii) 	The root and nullNodes (leaf markers) are BLACK
 * 		iii)	No RED nodes have RED children
 * 		iv) 	All root to leaf paths contain the same number of BLACK nodes
 * </p>
 * 
 * @author Simon Campbell <soxford1379@gmail.com> 
 * @version 1.0
 * @since 2015-02-16
 */
public class RedBlackTree<E extends Comparable<E>> {
	
	/**
	 * The header has no left child and the right child is the root of the tree
	 */
	private RedBlackNode<E> header;									// head of the tree
	
	/**
	 * The nullNode is the leaf node for the tree. All leafs are the nullNode
	 */
	private RedBlackNode<E> nullNode = new RedBlackNode<E>( null ); // null node for leaf pointers

	/**
	 * These pointers are used for tracking ancestors in routines whilst traversing the tree
	 */
	private  RedBlackNode<E> current;
	private  RedBlackNode<E> parent;
	private  RedBlackNode<E> grand;
	private  RedBlackNode<E> great;
	

	/**
	 * Constructor for the empty tree.
	 */
	public RedBlackTree( ) {
		header      = new RedBlackNode<E>( null );
		header.left = nullNode;
		header.right = nullNode;
		nullNode.parent = header;
	}

	/**
	 * Compares element with the element contained in node, using compareTo, with the
	 * caveat that if node is the header node, then element is always larger (and so search/insertion will go to the right).
	 * This routine is called for comparisons when it is possible that node is header.
	 * If it is not possible for node to be header, compareTo is used directly.
	 * @return integer with value > 0 if node is header or element is greater than t.element according to compareTo(), 
	 * 						value < 0 if element is less than t.element according to compareTo() 
	 * 						and 0 otherwise. 
	 * @param element the element to be compared to
	 * @param node the node whose element is compared to element unless node is header
	 */
	private final int compare( E element, RedBlackNode<E> node ) {
		if( node == header )
			return 1;
		else
			return element.compareTo( node.element );
	}

	/**
	 * Adds an element into the tree.
	 * @param element the element to insert.
	 * @throws DuplicateItemException if item is already present.
	 */
	public void add( E element ) throws DuplicateItemException {
		current = header;
		parent = header;
		grand = header;
		nullNode.element = element;

		while( compare( element, current ) != 0 ) {			// loop breaks when nullNode is found or non-nullNode containing element is found
			great = grand; grand = parent; parent = current;
			current = compare( element, current ) < 0 ?
					current.left : current.right;

			//check if two red children; fix if so
			if( current.left!= null 
					&& current.left.colour == RedBlackNode.Colour.RED 
					&& current.right != null 
					&& current.right.colour == RedBlackNode.Colour.RED ) {
				
				handleReorient( element );
			
			}
		}

		// Insertion fails if already present
		if( current != nullNode )
			throw new DuplicateItemException( element.toString( ) );
		current = new RedBlackNode<E>( element, nullNode, nullNode, parent );

		// Attach to parent
		if( compare( element, parent ) < 0 ) {
			parent.left = current;
		} else {
			parent.right = current;
		}
		handleReorient( element );
	}

	/**
	 * Removes an element from the tree.
	 * @param x the element to remove if it is in the tree.
	 * @return true if and only if the object was in the tree and removed
	 */
	public boolean remove( E x ) {
		if( x == null ) {
			return false;
		}

		//Find x in the tree if it exists - if not return false
		nullNode.element = x;
		current = header.right;	//initialize at the root, we can assume here on that current is not the header.
		//find node containing x if it exists
		for( ; ; ) {
			if( x.compareTo( current.element ) < 0 )
				current = current.left;
			else if( x.compareTo( current.element ) > 0 )
				current = current.right;
			else if( current != nullNode )
				break;
			else
				return false;
		}

		if ( current.right == nullNode || current.left == nullNode ) { // at least one nullNode child
			return delete_node_with_at_most_one_child(current);
		} else {
			//if both children are non-null then swap elements with successor and remove element again (reduces to an earlier case)
			RedBlackNode<E> succ = current.right;
			while (succ.left != nullNode) {
				succ = succ.left;
			}
			//swap elements with succesor
			E tmpVal = succ.element;
			succ.element = current.element;
			current.element = tmpVal;
			return remove(x);
		}
	}


	/**
	 * Helper method for removing a node from the tree when the node has at most one non-leaf child node
	 * <p>
	 * If the node and it's child are both BLACK then the work is forwarded to {@link #delete_case1(RedBlackNode)}   
	 * </p>
	 * @param node the node to remove
	 * @return true if and only if the node is removed
	 */
	private boolean delete_node_with_at_most_one_child(RedBlackNode<E> node) {
		/*
		 * Precondition: node has at most one non-null child.
		 */
		RedBlackNode<E> child = ( (node.right == nullNode) ? node.left : node.right );

		replace_node(node, child);
		if (node.colour == RedBlackNode.Colour.BLACK) {		//the RED case requires no further work to maintain invariants
			if (child.colour == RedBlackNode.Colour.RED){
				child.colour = RedBlackNode.Colour.BLACK;
			} else {
				return delete_case1(child);
			}
		}

		return true; 

	}

	/**
	 *  Helper method to splice out a node with at most one non-leaf child and replace it with that child
	 * @param node the node to be replaced/spliced out
	 * @param child the node to splice in
	 */
	private void replace_node(RedBlackNode<E> node, RedBlackNode<E> child) {
		if (node.parent.right == node) {
			node.parent.right = child;
		} else {
			node.parent.left = child;
		}

		if( child != nullNode) {
			child.parent = node.parent;
		}
	}
	
	/**
	 * Helper method to handle the case when our removal resulted in child being the root.
	 * In this case, we removed one BLACK node from every path, and the new root is BLACK, so the properties are preserved.
	 * 
	 * <p>
	 * 	Otherwise we forward to {@link #delete_case2(RedBlackNode)}
	 * </p>
	 * @param child the node below which invariants hold but above which invariants need to be maintained after the removed node is gone
	 * @return true if and only if the adjustment of the tree to maintain invariants is successful
	 */
	private boolean delete_case1(RedBlackNode<E> child) {
		if (child.parent != header){ //checks we are not at the root
			return delete_case2(child);
		}

		return true;
	}

	/**
	 * Helper method to handle the case when the new sibling of child S is {@link RedBlackNode.Colour#RED}. 
	 * <p>In this case we reverse the colors of the parent and sibling, and then rotate at the parent, turning the sibling 
	 * into child's grandparent. Note that the parent has to be BLACK as it had a RED child. 
	 * Although all paths still have the same number of BLACK nodes, 
	 * now child has a BLACK sibling and a RED parent, so we can proceed to steps 4, 5, or 6. (Its new sibling is BLACK because it was once the child of the RED S.) 
	 * </p>
	 * <p>
	 * 	Once adjusted we forward to {@link #delete_case3(RedBlackNode)} to handle the relevant case now that we know child's sibling is BLACK
	 * </p>
	 * 
	 * @param child the node below which invariants hold but above which invariants need to be maintained after the removed node is gone
	 * @return true if and only if the adjustments of the tree to maintain invariants is successful
	 */
	private boolean delete_case2(RedBlackNode<E> child)
	{
		RedBlackNode<E> s = sibling(child);

		if (s.colour == RedBlackNode.Colour.RED) {
			child.parent.colour = RedBlackNode.Colour.RED;
			s.colour = RedBlackNode.Colour.BLACK;
			if (child == child.parent.left)
				rotate_left(child.parent);
			else
				rotate_right(child.parent);
		}
		return delete_case3(child);
	}

	/**
	 * Helper method to find the sibling of a given node 
	 * @param child the node whose sibling is to be found
	 * @return the sibling node
	 */
	private RedBlackNode<E> sibling(RedBlackNode<E> child)
	{
		if (child == child.parent.left)
			return child.parent.right;
		else
			return child.parent.left;
	}

	/**
	 *  Helper to handle case when the parent, sibling, and sibling's children are all {@link RedBlackNode.Colour#BLACK}. 
	 *  <p>In this case, we simply repaint the sibling RED. The result is that all paths passing through the sibling, 
	 *  which are precisely those paths not passing through child, have one less BLACK node. 
	 *  Because deleting child's original parent made all paths passing through child have one less BLACK node, 
	 *  this evens things up. However, all paths through the parent now have one fewer BLACK node than paths 
	 *  that do not pass through the parent, so invariant iv) (all paths from any given node to its leaf nodes 
	 *  contain the same number of BLACK nodes) is still violated. To correct this, we perform the 
	 *  re-balancing procedure on parent, starting at {@link #delete_case1(RedBlackNode)}. 
	 *  </p>
	 *  <p> In other cases we forward onto {@link #delete_case4(RedBlackNode)}
	 *  
	 * @param child the node below which invariants hold but above which invariants need to be maintained after the removed node is gone
	 * @return true if and only if the adjustments of the tree to maintain invariants is successful
	 */
	private boolean delete_case3(RedBlackNode<E> child)
	{
		RedBlackNode<E> s = sibling(child);

		if ((child.parent.colour == RedBlackNode.Colour.BLACK) &&
				(s.colour == RedBlackNode.Colour.BLACK) &&
				(s.left.colour == RedBlackNode.Colour.BLACK) &&
				(s.right.colour == RedBlackNode.Colour.BLACK)) {
			s.colour = RedBlackNode.Colour.RED;
			return delete_case1(child.parent);
		} else
			return delete_case4(child);
	}

	/**
	 * Helper to handle case when child's sibling and sibling's children are {@link RedBlackNode.Colour#BLACK}, but its parent is {@link RedBlackNode.Colour#RED}. 
	 * <p>In this case, we simply exchange the colours of S and P. This does not affect the number of black nodes 
	 * on paths going through S, but it does add one to the number of black nodes on paths going through child, 
	 * making up for the deleted black node on those paths.
	 * </p>
	 * <p>
	 * Otherwise forward to {@link #delete_case5(RedBlackNode)}
	 * </p>
	 * @param child the node below which invariants hold but above which invariants need to be maintained after the removed node is gone
	 * @return true if and only if the adjustments of the tree to maintain invariants is successful
	 */
	private boolean delete_case4(RedBlackNode<E> child)
	{
		RedBlackNode<E> s = sibling(child);

		if ((child.parent.colour == RedBlackNode.Colour.RED) &&
				(s.colour == RedBlackNode.Colour.BLACK) &&
				(s.left.colour == RedBlackNode.Colour.BLACK) &&
				(s.right.colour == RedBlackNode.Colour.BLACK)) {
			s.colour = RedBlackNode.Colour.RED;
			child.parent.colour = RedBlackNode.Colour.BLACK;
			return true;
		} else
			return delete_case5(child);
	}

	/**
	 *   Helper to handle case where child's sibling is {@link RedBlackNode.Colour#BLACK}, the sibling's left child is {@link RedBlackNode.Colour#RED}, 
	 *   the sibling's right child is BLACK, and child is the left child of its parent, or the mirror image of this case.
	 *   <p>
	 *   In this case we rotate right (or left) at the sibling, so that the sibling's left (right) child becomes the sibling's parent and the childs's new sibling. 
	 *   We then exchange the colours of the former sibling and its new parent. All paths still have the same number of black nodes, but now the child has a BLACK sibling whose right 
	 *   (left) child is red, so we fall into case 6. Neither the child nor its parent are affected by this transformation. 
	 * 	 </p>
	 * 	 <p>
	 * 	 Once this case is excluded we forward to {@link #delete_case6(RedBlackNode)}
	 *   </p> 
	 * @param child the node below which invariants hold but above which invariants need to be maintained after the removed node is gone
	 * @return true if and only if the adjustments of the tree to maintain invariants is successful
	 */

	private boolean delete_case5(RedBlackNode<E> child)
	{
		RedBlackNode<E> s = sibling(child);

		if  (s.colour == RedBlackNode.Colour.BLACK) { 
			/* this if statement is trivial,
				due to case 2 (even though case 2 changed the sibling to a sibling's child,
				the sibling's child can't be RedBlackNode.Colour.RED, since no RedBlackNode.Colour.RED parent can have a RedBlackNode.Colour.RED child).
			 	the following statements just force the RedBlackNode.Colour.RED to be on the left of the left of the parent,
	   			or right of the right, so case six will rotate correctly. */
			if ((child == child.parent.left) &&
					(s.right.colour == RedBlackNode.Colour.BLACK) &&
					(s.left.colour == RedBlackNode.Colour.RED)) { /* this last test is trivial too due to cases 2-4. */
				s.colour = RedBlackNode.Colour.RED;
				s.left.colour = RedBlackNode.Colour.BLACK;
				rotate_right(s);
			} else if ((child == child.parent.right) &&
					(s.left.colour == RedBlackNode.Colour.BLACK) &&
					(s.right.colour == RedBlackNode.Colour.RED)) {/* this last test is trivial too due to cases 2-4. */
				s.colour = RedBlackNode.Colour.RED;
				s.right.colour = RedBlackNode.Colour.BLACK;
				rotate_left(s);
			}
		}
		return delete_case6(child);
	}

	/**
	 * Helper for the final case where the sibling is {@link RedBlackNode.Colour#BLACK}, the sibling's 
	 * right child is {@link RedBlackNode.Colour#RED}, and the child is the left child of its parent (or the mirror image). 
	 * 
	 * <p>In this case we rotate left (right) at the parent, so that the sibling becomes the parent of the parent of child
	 * and the original sibling's right (left) child. 
	 * We then exchange the colours of the parent and original sibling, and make the sibling's right (left) child BLACK. 
	 * The subtree still has the same colour at its root, so Properties iii) (Both children of every 
	 * red node are black) and iv) (All paths from any given node to its leaf nodes contain the same 
	 * number of black nodes) are not violated. However, the child now has one additional black ancestor: 
	 * either the  parent has become BLACK, or it was black and the original sibling was added as a BLACK grandparent. 
	 * Thus, the paths passing through the child pass through one additional BLACK node.
	 * Meanwhile, if a path does not go through the child, then there are two possibilities:
	 * </p>
	 * 
	 * <p>It goes through childs's new sibling. Then, it must go through the original sibling and parent, both formerly 
	 * 	and currently, as they have only exchanged colours and places. Thus the path contains the same number of black nodes.
	 * </p>
	 * 
	 * <p>It goes through child's new uncle, the original sibling's right (left) child. 
	 * 	Then, it formerly went through the original sibling, the sibling's parent, and the sibling's right (left) child (which was RED), 
	 *  but now only goes through the original sibling, which has assumed the colour of its former parent, and the original sibling's 
	 *  right (left) child, which has changed from RED to BLACK (assuming the original sibling's colour: BLACK). 
	 *  The net effect is that this path goes through the same number of BLACK nodes.
	 * </p>
	 *  
	 * <p>Either way, the number of BLACK nodes on these paths does not change. 
	 * Thus, we have restored Properties iii) (Both children of every red node are black) and iv) 
	 * (All paths from any given node to its leaf nodes contain the same number of black nodes). 
	 * </p>
	 * @param child the node below which invariants hold but above which invariants need to be maintained after the removed node is gone
	 * @return true if and only if the adjustments of the tree to maintain invariants is successful
	 */
	private boolean delete_case6(RedBlackNode<E> child)
	{
		RedBlackNode<E> s = sibling(child);

		s.colour = child.parent.colour;
		child.parent.colour = RedBlackNode.Colour.BLACK;

		if (child == child.parent.left) {
			s.right.colour = RedBlackNode.Colour.BLACK;
			rotate_left(child.parent);
		} else {
			s.left.colour = RedBlackNode.Colour.BLACK;
			rotate_right(child.parent);
		}

		return true;
	}


	/**
	 * Find the smallest element in the tree.
	 * @return the smallest element or null if empty.
	 */
	public E findMin( ) {
		if( isEmpty( ) )
			return null;

		RedBlackNode<E> itr = header.right;

		while( itr.left != nullNode )
			itr = itr.left;

		return itr.element;
	}

	/**
	 * Find the largest element in the tree.
	 * @return the largest element or null if empty.
	 */
	public E findMax( ) {
		if( isEmpty( ) )
			return null;

		RedBlackNode<E> itr = header.right;

		while( itr.right != nullNode )
			itr = itr.right;

		return itr.element;
	}

	/**
	 * Find the next element in the tree.
	 * @return the next element after x or null if x not in tree or no such successor element exists because x is the maximum.
	 */
	public E findSuccessor(E x){
		nullNode.element = x;
		current = header.right;
		//find node containing x if it exists
		for( ; ; ) {
			if( x.compareTo( current.element ) < 0 )
				current = current.left;
			else if( x.compareTo( current.element ) > 0 )
				current = current.right;
			else if( current != nullNode )
				break;
			else
				return null;
		}

		if ( current.right != nullNode ) { 	// successor is below x in tree
			//find min right child
			current = current.right;
			while(current.left != nullNode){
				current = current.left;
			}

			return current.element;
		} else {							// successor must be above x in the tree if it exists
			//find last left move in ancestry

			//find last left move before current if it exists
			while ( current.parent != header ) {		// search up the tree until we have nowhere to go
				if( current.parent.left == current ){	// if we find a left move then return the element leading to the left move
					return current.parent.element;
				} else {
					current = current.parent;			// else keep searching
				}
			}

			return null;								// no left move was found so no successor element

		}


	}

	/**
	 * Find an element in the tree.
	 * @param x the element to search for.
	 * @return true if and only if the element is found in the tree.
	 */
	public boolean contains( E x ) {
		nullNode.element = x;
		current = header.right;

		for( ; ; ) {
			if( x.compareTo( current.element ) < 0 )
				current = current.left;
			else if( x.compareTo( current.element ) > 0 )
				current = current.right;
			else if( current != nullNode )
				return true;
			else
				return false;
		}
	}

	/**
	 * Removes all of the elements from this tree. The tree will be empty after this call returns.
	 */
	public void clear( ) {
		header.right = nullNode;
	}

	/**
	 * Print all items.
	 */
	public void printTree( ) {
		printTree( header.right );
	}

	/**
	 * Internal method to print a subtree in order.
	 * @param t the node that roots the tree to print.
	 */
	private void printTree( RedBlackNode<E> t ) {
		if( t != nullNode ) {
			printTree( t.left );
			System.out.println( t.element );
			printTree( t.right );
		}
	}

	/**
	 * Returns true if this set contains no elements
	 * @return true if this set contains no elements
	 */
	public boolean isEmpty( ) {
		return header.right == nullNode;
	}

	/**
	 * Internal routine that is called during an insertion
	 * if a node has two red children. Performs flip and rotations.
	 * @param element the item being inserted.
	 */
	private void handleReorient( E element ) {
		// Do the colour flip
		current.colour = RedBlackNode.Colour.RED;
		current.left.colour = RedBlackNode.Colour.BLACK;
		current.right.colour = RedBlackNode.Colour.BLACK;

		if( parent.colour == RedBlackNode.Colour.RED )   // Have to rotate
		{
			grand.colour = RedBlackNode.Colour.RED;		//assumes R-B invariants hold above current
			if( ( compare( element, grand ) < 0 ) !=
					( compare( element, parent ) < 0 ) )
				parent = rotate( element, grand );  // Start dbl rotate
			current = rotate( element, great );
			current.colour = RedBlackNode.Colour.BLACK;
		}
		header.right.colour = RedBlackNode.Colour.BLACK; // Ensure root is black
	}

	/**
	 * Internal routine that performs a single or double rotation.
	 * Because the result is attached to the parent, there are four cases.
	 * Called by handleReorient.
	 * @param element the item in handleReorient.
	 * @param parent the parent of the root of the rotated subtree - assumes parent has sufficient non-null descendants
	 * @return the root of the rotated subtree.
	 */
	private RedBlackNode<E> rotate( E element, RedBlackNode<E> parent ) {
		if( compare( element, parent ) < 0 ) {
			return parent.left = ( compare( element, parent.left ) < 0 
					? rotate_right( parent.left )    	// LL
							: rotate_left( parent.left ));  	// LR
		} else {
			return parent.right = ( compare( element, parent.right ) < 0 
					? rotate_right( parent.right )   	// RL
							: rotate_left( parent.right ) );  	// RR
		}
	}

	/**
	 * Rotate binary tree right at the node.
	 * Assumes that node.left is not the nullNode (so node.left.right is non-null)
	 * @param node the node at which the right rotation occurs
	 * @return the root of the rotated tree
	 */
	private RedBlackNode<E> rotate_right( RedBlackNode<E> node ) {
		RedBlackNode<E> child = node.left; 
		node.left = child.right;		// redirect k2.left
		node.left.parent = node;	// update parenthood of k2.left
		child.right = node;			// redirect k1.right
		child.parent = node.parent;	//update k1 parent
		node.parent = child;			// update parenthood of k1.right (== k2)
		return child;
	}

	/**
	 * Rotate binary tree left at the node.
	 * Assumes that node.right is not the nullNode (so node.right.left is non-null)
	 * @param node the node at which the right rotation occurs
	 * @return the root of the rotated tree
	 */
	private RedBlackNode<E> rotate_left( RedBlackNode<E> node ) {
		RedBlackNode<E> child = node.right;
		node.right = child.left; 	// redirect k1.right
		node.right.parent = node;	// update parenthood of k1.right 
		child.left = node;			// redirect k2.left
		child.parent = node.parent;	// update k2 parent
		node.parent = child;			// update parenthood of k2.left (== k1)
		return child;
	}

	/**
	 * Class RedBlackNode is instantiated as nodes in the RedBlackTree
	 * @author Simon Campbell <soxford1379@gmail.com>
	 * 
	 */
	private static class RedBlackNode<E extends Comparable<E>> {

		E   element;    // The data in the node
		RedBlackNode<E> left;       // Left child
		RedBlackNode<E> right;      // Right child
		RedBlackNode<E> parent;      // parent node
		Colour          colour;      // Colour
		
		/** 
		 * The Colour enum enumerates the possible colourings of the TreeNodes
		 * @author Simon Campbell <soxford1379@gmail.com>
		 *
		 */
		public static enum Colour { BLACK,  RED }						//colouring options

		// Constructors
		RedBlackNode( E theElement ) {
			this( theElement, null, null, null );
		}

		RedBlackNode( E theElement, RedBlackNode<E> lt, RedBlackNode<E> rt, RedBlackNode<E> parent ) {
			element  = theElement;
			left     = lt;
			right    = rt;
			this.parent = parent;
			colour    = Colour.BLACK;
		}
	}





}

