% This file includes all rules of prolog
% Copy this file to output directory

%client(_,_,_,_,_,_,_,_).
%node(_,_,Lid1,Nid,_).
%taxi(_,_,_,_,_,_,_,_,_,_,_).
%line(_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_).
%traffic(_,_,_).

onenode(X,Y,Lid,Nid,_,H1,H2):-
	node(X,Y,Lid,Nid,_,H1,H2),!.

canGoAll(Nid1,Np,_,X,_):-
	findall(	Nid2/D/H,	can_go(Nid1,_,Np,Nid2,D,H),	X).
%	modify(X,Nss,Dss,Hss).


can_go(Nid1,_,Nidp,Nid2,D,H):-
	allown(Nid1,Nid2,D),
	Nidp =\= Nid2,
	onenode(_,_,_,Nid2,_,H,_).
%	\+ Nid1=Nid2,
%	euclDistance(X1,Y1,X2,Y2,D),
%	distance(Nid1,Nid2,L,D1,D),
%	hscore(X2,Y2,Nid2,L,Ntarg,H).


%modify([],[],[],[]):-
%modify([(N,D,H)|Xs],[N|Ns],[D|Ds],[H|Hs]):-
%	modify(Xs,Ns,Ds,Hs).
	

canGoAll2(Nid1,Nidp,Ntarg,Xss,FoundN):-
( can_go1(Nid1,Nidp,Ntarg,N,D,H,FoundN)->
(
  canGoAll2(Nid1,Nidp,Ntarg,Xs,[N|FoundN]),
  Xss=[N/D/H|Xs]
)
  ;
  Xss=[]
).

can_go2(Nid1,_,Nid2,D,H,FoundN):-
  allown(Nid1,Nid2,D),
  \+ member(Nid2,FoundN),
  onenode(_,_,_,Nid2,_,H,_).

canGoAll1(Nid1,Ntarg,Nss,Dss,Hss,FoundN):-
( can_go1(Nid1,Ntarg,N,D,H,FoundN)->
(
% canGoAll1(Nid1,Ntarg,Ns,Hs,Ds,[N|FoundN]),
  Ns=[], Ds=[], Hs=[],
  Nss=[N|Ns],Dss=[D|Ds],Hss=[H|Hs]
)
  ;
  Nss=[], Dss=[], Hss=[]
).

can_go1(Nid1,Nidp,_,Nid2,D,H,FoundN):-
  allown(Nid1,Nid2,D),
  Nidp =\= Nid2,
  \+ member(Nid2,FoundN),
  onenode(_,_,_,Nid2,_,H,_).


allown(Nid1,Nid2,D):-
	(
	next(Nid1,Nid2,D);	next(Nid2,Nid1,D)
	).


% distance(_,_,_,D,D).

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

