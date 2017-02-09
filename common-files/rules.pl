% This file includes all rules of prolog
% Copy this file to output directory

%client(_,_,_,_,_,_,_,_).
%node(_,_,Lid1,Nid,_).
%taxi(_,_,_,_,_,_,_,_,_,_,_).
%line(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_).
%traffic(_,_,_).

% We precompute the cost of each hop (by consulting lines.pl while writing nextt.pl)
% Same way we have excluded one-way or footways from nextt.pl
% We also precompute the heuristic function of each node, so that its ready when needed.
% This saves much time making our querries much faster
% If we want this querry to do everything we should use canMoveFromSlow but that`s too slow

canMoveFrom(From,Np,To,D):-
	next(From,To,D),
	Np =\= To.

% find all solution at once
canMoveAll(From,Np,_,X,_):-
	findall(	To/D/H,	canMoveFromSlow(From,Np,To,D,H),	X).

onenode(X,Y,Lid,Nid,_,H1,H2):-
	node(X,Y,Lid,Nid,_,H1,H2),!.

canMoveFromSlow(From,Np,To,D,H):-
	next(From,To,D),
	Np =\= To,
	onenode(_,_,_,To,_,H,_).
	

allown(Nid1,Nid2,D,Np):-
	(
	next(Nid1,Nid2,D),
	Np =\= Nid2
	).

hscore(X2,Y2,_,_,Ntarg,H):-
%	node(Xtarg,Ytarg,L,_,_),
	distanceToNode(X2,Y2,Ntarg,H).

belongTo(X,Y):-node(_,_,X,Y,_).

euclDistance(X1,Y1,X2,Y2,D):-
	D is sqrt((X1-X2)^2+(Y1-Y2)^2).

findClosestNode(X1,Y1,Nid,D):-
	node(X2,Y2,_,Nid,_,_,_),
	euclDistance(X1,Y1,X2,Y2,D).

distanceToNode(X1,Y1,Nid,D):-
	node(X2,Y2,_,Nid,_),
	euclDistance(X1,Y1,X2,Y2,D),!.

%	following doesn`t work for JIProlog. 
%	Not in prolog ISO and only implemented by swi-pl
closestNode(X,Y,Nid,D1):-
	aggregate(min(D,(Nid1,D)),distanceToNode(X,Y,Nid1,D),min(_,(Nid,D1))).

