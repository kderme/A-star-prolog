% This file includes all rules of prolog
% Copy this file to output directory

%client(_,_,_,_,_,_,_,_).
%node(_,_,Lid1,Nid,_)
%taxi(_,_,_,_,_,_,_,_,_,_,_).
%line(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_).
%traffic(_,_,_).

can_go(Nid1,Ntarg,Nid2,D,H):-
	allown(Nid1,Nid2),
	node(X1,Y1,L,Nid1,_),
	node(X2,Y2,L,Nid2,_),
	\+ Nid1=Nid2,
	euclDistance(X1,Y1,X2,Y2,D),
%	distance(Nid1,Nid2,L,D1,D),
	hscore(X2,Y2,Nid2,L,Ntarg,H).

allown(Nid1,Nid2):-
	next(Nid1,Nid2).

% distance(_,_,_,D,D).

hscore(X2,Y2,_,_,Ntarg,H):-
%	node(Xtarg,Ytarg,L,_,_),
	distanceToNode(X2,Y2,Ntarg,H).

belongTo(X,Y):-node(_,_,X,Y,_).

euclDistance(X1,Y1,X2,Y2,D):-
	D is sqrt((X1-X2)^2+(Y1-Y2)^2).

findClosestNode(X1,Y1,Nid,D):-
	node(X2,Y2,_,Nid,_),
	euclDistance(X1,Y1,X2,Y2,D).

distanceToNode(X1,Y1,Nid,D):-
	node(X2,Y2,_,Nid,_),
	euclDistance(X1,Y1,X2,Y2,D),!.

%	following doesn`t work for JIProlog. 
%	Not in prolog ISO and only implemented by swi-pl
closestNode(X,Y,Nid,D1):-
	aggregate(min(D,(Nid1,D)),distanceToNode(X,Y,Nid1,D),min(_,(Nid,D1))).

