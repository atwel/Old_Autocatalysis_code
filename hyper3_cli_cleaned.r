## Absolutely required packages:
library(igraph)
library(sna,warn.conflicts=FALSE)

## Create a default parameter list.
## This function generates a generic default argument
## list that can be specified at call time or edited
## after the fact.
hyper3params <- function(
    ## explored parameters of the model            
    maxBallType=8,          
    initialEdges="moore",   ## c("moore","random")
    reproduction="source",  ## c("source","target","reciprocal")   
    chemistry="all",        ## c("all","solo")
    searchType="random",    ## c("random","selective")
    env.endogenous=TRUE,    
    env.rich=FALSE,
    
    
    ## unexplored but possibly consequential parameters of the model
    n=100, 
    maxBallsInSystem=200,   
    maxEdgesInSystem=n*8,
    numRulesType="fixed", ##c("fixed", "variable")  
    numRulesEachType=20,
    numRulesTotal=200,
    env.startingBalls=200   ## ignored if !env.endogenous
)
{
    return(as.list(sys.frame(sys.nframe())))
}



###########################
## newWorld()
##  Creates a new 'world' object based on the given parameters.
##  This object is a classed list() with parameter values and other variables
##  describing the state of the entire simulation environment, bins and
##  inventories included.
##  After creation, functions like iterWorld() and (more commonly) runWorld()
##  are used to push the world through ticks.
###########################

newWorld <- function(params=hyper3params()){
    
    ## all the work is done within parameter list, so 
    ## make sure it's a list and classed right
    params <- as.list(params)
    class(params) <- c("hyper3world","list")
    
    ## with the proper parameter list, we use it to
    ## start setting up a world
    within(params,{ 
    
        ############
        ## tick: current time step
        ############
        tick <- 0 ## brand new world        
        

        ############
        ## initialize up address inventories. These are put into an
        ## adjacency matrix of counts.
        ############

        ## 'am' holds the address inventory matrix.
        ## if initialEdges=='moore', give everybody one address to each of
        ## their Moore neighbors. If initialEdges=='random', then distribute
        ## maxEdgesInSystem edges uniformly and independently among possible
        ## edges (multiples allowed). If initialEdges=='full', then every 
        ## possible edge is realized.
        am <-  as.matrix(get.adjacency(agraph.moore(n)))
        

        ############
        ## set up the Urn of balls, called ballEnv
        ## ballEnv is a numeric vector, the i'th element of which
        ## is the count (possible Inf) of the i'th ball.
        ############
        
        ## first set ballEnv assuming endogenous environment 
        ballEnv <- 
            if(env.rich){
                ## if rich environment, all counts are same
                rep(env.startingBalls,maxBallType)
            }else{
                ## otherwise first count is positive, rest are zero
                c(env.startingBalls,rep(0,maxBallType-1))
            }
            
        ## now if environment is not endogenous, set any positive counts
        ## to Inf
        if(!env.endogenous){
            ballEnv[ballEnv>0] <- Inf
        }
        
        ## one last detail.
        ## infinite values don't work in `prob` argument to sample, so we make this:
        probBallEnv <- if(env.endogenous){
            ballEnv
        }else{
            is.infinite(ballEnv)
        }
        
        
        ############
        ## Create rule types based on chemistry. This is a numRules x 2
        ## matrix giving input ball and output ball labels. Inventories are
        ## handled next.
        ############
        
        if(chemistry=="all"){ # 'all' chemistry
            ## combn() gives all (i,j) combos where i<j (half of what we want),
            ## so we combine its ouput with its mirror image to get all rules.
            ruleList <- t(combn(maxBallType,2))
            ruleList <- rbind(ruleList,ruleList[,2:1])
            colnames(ruleList) <- c("inBall","outBall") ## be sure to name cols
            
        }else if(chemistry=="solo"){ # 'solo' chemistry
            ## solo rule list is simple to construct
            ruleList <- cbind(inBall=1:maxBallType,outBall=c(2:maxBallType,1))
            
        }else{ # catch for misspecified chemistry
            stop("don't know what to do with chemistry type '",chemistry,"'.")
        }
        


        ############
        ## Make rule inventory for each bin.
        ## Represented by an n-by-numRules matrix with counts
        ############
        
        ## start out empty
        ruleInv <- matrix(0,nrow=n,ncol=nrow(ruleList))
		
        ## Now we need to figure out how many rules to give to each bin
        numRules <-switch(numRulesType, 
			fixed = floor(200/(maxBallType**2 - maxBallType)),
			variable = numRulesEachType)
			
        ## For each rule type, distribute a number of rules to the bins
        ## uniformly and independently (multiples allowed).
        ## Number of rules distributed is numRulesEachType.

        l <- c()
        for (i in c(1:nrow(ruleList))){
 			l <- c(l,rep(i,numRules))      	
        }
        ## if we have a fixed number of rules and the number of rule types
        ## is not a divisor, we need to distribute some extras to get up 
        ## to the right count. 
        if ((floor(200/(maxBallType**2 - maxBallType)) * nrow(ruleList)) != numRulesTotal){
        	exs <- (numRulesTotal - (numRules * nrow(ruleList)))
        	l <- c(l,sample(1:nrow(ruleList),exs))      		
        	}
        for (i in l){
        	cell <- sample(1:n,1)
        	ruleInv[cell,i] <- ruleInv[cell,i] + 1
        }
     
	
        	
        ############
        ## Create a logical vector indicating which bins are living.
        ## 'Living' is defined here as having at least one out-neighbor
        ## (ie at least one address) _or_ at least one rule.
        ############

        isAlive <- rowSums(am) | rowSums(ruleInv)
        
        ############
        ## These are utility variables to make reproduction easier later.
        ## Both get set to TRUE if reproduction is "reciprocal".
        ############
        reproduceSource <- reproduction %in% c("source","reciprocal")
        reproduceTarget <- reproduction %in% c("target","reciprocal")
        print('World created')
    })
}



###########################
## iterWorld()
##  This is the bulk of the model logic. It takes a 'world' list (as output
##  by newWorld, iterWorld, runWorld, etc), and runs it through niter ticks.
##  It is designed for speed rather than feedback, so most printing/plotting
##  is done through runWorld function below.
###########################

iterWorld <- function(world,niter=1){
    ############
    ## For convenience it is allowed to feed just a parameter list
    ## to iterWorld, which it will then convert to a full world using
    ## newWorld.
    ############
    
    if(is.null(world$ruleInv)){ ### changed to ruleInv instead of ballInv
        world <- newWorld(world)
    }

    ############
    ## Do all the work inside the 'world' list, immediately returning the
    ## result.
    ############
    
    within(world,{
    	insidetick <- 0
        while(insidetick <= niter){ ## do all of this niter times
            insidetick <- insidetick + 1
            ## increment tick
            tick <- tick+1
            


            ############
            ## Set curBall to NULL at the beginning of each tick
            ## (ball 'in hand')
            ############
            curBall <- NULL
            
            
            ############
            ## pick a bin and a rule randomly, weighted by rule count.
            ## Do this by picking a single (row,col) from ruleInv, weighted
            ## by the count in that (row,col). Then translate this to a bin
            ## number and a rule number.
            ############
            
            thisInd <- sample(length(ruleInv),1,prob=ruleInv)
            thisBin <- row(ruleInv)[thisInd]
            thisRule <- col(ruleInv)[thisInd]
            
            
            ############
            ## Try to get thisRule's input ball from environment
            ############
            
            
            ## leave curBall as NULL *unless* the following statement holds
            if(
                (
                    searchType=="selective" &&      ## (selective search AND available)
                    ballEnv[ruleList[thisRule,"inBall"]]>0
                ) ||                                ## OR
                (
                    searchType=="random" &&         ## (random search AND lucky)
                    sample(maxBallType,1,prob=probBallEnv) == ruleList[thisRule,"inBall"]
                )
            ){
                ## take the ball 'in hand' (curBall)
                curBall <- ruleList[thisRule,"inBall"]
                if(env.endogenous){
                    # remove from ballEnv by decrementing count
                    ballEnv[curBall] <- ballEnv[curBall]-1
                    ## update probBallEnv
                    probBallEnv <- ballEnv
                }
            }
             
            
            ############
            ## As long as we have a ball (curBall is not NULL),
            ## keep passing it around.
            ############
            
            while(!is.null(curBall)){
            	
                ############
                ## First convert the ball according to the rule.
                ############
                curBall <- ruleList[thisRule,"outBall"]
                   
                
                ############
                ## If thisBin has no neighbors, throw the converted ball
                ## ball back (if endogenous env), otherwise pass it on.
                ############
                
                if(!any(am[thisBin,]>0)){ ## thisBin has no neighbors
                    ## throw it back if appropriate
                    if(env.endogenous){
                        ballEnv[curBall] <- ballEnv[curBall] + 1
                        probBallEnv <- ballEnv
                    }
                    ## set curBall to NULL to stop the loop
                    curBall <- NULL
                }else{
                    ############
                    ## thisBin has at least one neighbor, so we try to make
                    ## a pass.
                    ############
                    
                    ## which rules are compatible with curBall as input?
                    compatRules <- which(ruleList[,"inBall"]==curBall)
                    
                    ############
                    ## Pick a neighbor, weighted by address inventory.
  	         ############

                    nextBin <- sample(n,1,prob=am[thisBin,])

                   
                    ############
                    ## We check if the neighbor has a compatible rule.
                    ############
                    if(!any(ruleInv[nextBin,compatRules]>0)){
                        ## incompatible, toss into environment.
                        if(env.endogenous){
                            ballEnv[curBall] <- ballEnv[curBall] + 1
                            probBallEnv <- ballEnv
                        }
                        curBall <- NULL
                    }

                    ############
                    ## If curBall is still not NULL, then there was a successful pass
                    ## (either brokered or not). So go through and reproduce edges and rules
                    ## as appropriate, and kill off edges and rules as appropriate.
                    ############
                    
                    if(!is.null(curBall) && thisBin != nextBin){
                        ############
                        ## First pick which of the possibly several compatible rules is the one 
                        ## that will be used.
                        ############
                        ## the following convoluted if-else just picks a compatible rule from nextBin
                        nextRule <- if(length(compatRules)==1){ 
                            compatRules[1]
                        }else{
                            sample(
                                seq.int(nrow(ruleList))[compatRules],
                                1,prob=ruleInv[nextBin,compatRules]
                            )
                        }


                        ############
                        ## Now reproduce and kill off Rules, as appropriate.
                        ############
                        if(reproduceTarget){ ## target or reciprocal reproduction
                            ## increment count of nextRule in nextBin
                            ruleInv[nextBin,nextRule] <- ruleInv[nextBin,nextRule] + 1
                        }

                        if(reproduceSource){ ## source or reciprocal reproduction
                            ## increment count of thisRule in thisBin
                            ruleInv[thisBin,thisRule] <- ruleInv[thisBin,thisRule] + 1
                        }
                        
                           ## pick a random rule globally to kill
                        toDelete <- sample(length(ruleInv),1,prob=ruleInv)
                            ## decrement count of rule to kill
                        ruleInv[toDelete] <- ruleInv[toDelete] - 1


                        ############
                        ## Everything is complete for this pass, just need to prepare for the next pass.
                        ############
                        thisBin <- nextBin
                        thisRule <- nextRule
                    }
                }
                }
        
        } ## end giant for loop
        
    }) ## end within()
}


###########################
## Supplementary model logic
##  The following functions implement supplementary model logic, useful
##  both while running the model and in analysis.
###########################


## Creates a graph from living rule-bins, directed edges defined
## by rule- and address-compatibility.
## (Returns NA for too dense a network)
getCompatibilityMatrix <- function(w,cmSizeCutoff=200){
    cm <- w$am ## start with the whole adjacency matrix
    rownames(cm) <- colnames(cm) <- seq.int(nrow(cm)) ## give each row a name

    binRuleCounts <- rowSums(w$ruleInv>0) ## how many rules living in each bin?
    ## new indices, one node for each rulexbin combo with at least one alive
    newInd <- unlist(mapply(rep,x=seq.int(nrow(cm)),times=binRuleCounts))
    
    ## after this, cm is just a rulexbin adjacency matrix, ignoring
    ## compatibility (edges based on bin adjacency only)
    cm <- cm[newInd,newInd]
    
    ## see if it's just way too big
    if(length(cm)>1 && nrow(cm)>cmSizeCutoff) return(NULL)
    
    ## negate rulexbin edges with incompatible rules
    
    ## for each i,j combo that 'makes sense'
    for(i in unique(as.numeric(rownames(cm)))){
    for(j in which(w$am[i,]>0 & binRuleCounts>0)){
        ## for each i -> j rule combo check compatibility
        for(ii in seq.int(length(iRules <- which(w$ruleInv[i,]>0)))){
        for(jj in seq.int(length(jRules <- which(w$ruleInv[j,]>0)))){
            if(
                w$ruleList[iRules[ii],"outBall"] !=
                w$ruleList[jRules[jj],"inBall"]
            ){
                cm[
                    which(rownames(cm)==as.character(i))[ii],
                    which(colnames(cm)==as.character(j))[jj]
                ] <- 0
            }
        }}
    }}
    return(cm)
}

## translates output of getCompatibiltyMatrix by collapsing rules that share
## a bin and optionally filling out empty bins
getBinCompatibilityMatrix <- function(w,with.isolates=TRUE,binarize=FALSE){
    ## circumvent compatibility matrix if possible:
    if(binarize){
        bcm <- getBasicBinCompatibilityMatrix(w)
        if(!with.isolates){
            keepMe <- apply(bcm>0,1,any) | apply(bcm>0,2,any)
            bcm <- bcm[keepMe,keepMe]
        }
    }
    
    if(is.null(w$cm)){
        w$cm <- getCompatibilityMatrix(w)
    }
    
    ## first a degenerate case
    if(length(w$cm)<=1){ ## empty or singular compatibility matrix
        return(matrix(0,nrow=nrow(w$am),ncol=ncol(w$am)))
    }
    ## and another
    #print("down to colnams")
    if(length(unique(colnames(w$cm)))<=1){
        return(matrix(0,nrow=nrow(w$am),ncol=ncol(w$am)))
    }
    ## now collapse w$cm
    dupedNames <- duplicated(rownames(w$cm))
    multiBins <- unique(rownames(w$cm)[dupedNames])
    bcm <- w$cm
    for(b in multiBins){
        bcm[b,] <- colSums(w$cm[rownames(w$cm)==b,])
        bcm[,b] <- rowSums(w$cm[,colnames(w$cm)==b])
    }
    bcm <- bcm[!dupedNames,!dupedNames]

    if(with.isolates){
        emptyBins <- seq.int(w$n)[-as.numeric(rownames(bcm))]
        ## add missing columns and give names
        bcm <- cbind(bcm,matrix(0,nrow=nrow(bcm),ncol=length(emptyBins)))
        bcm <- rbind(bcm,matrix(0,nrow=length(emptyBins),ncol=ncol(bcm)))
        ## reorder for consistency
        #bcm <- bcm[order(as.numeric(rownames(bcm))),order(as.numeric(colnames(bcm)))]
    }
    
    if(binarize){
        bcm[bcm>1] <- 1
    }

    return(bcm)
}

## this function makes a bin compatibility matrix if world is too complex
## for getCompatibilityMatrix. Assumes `with.isolates' and `binarize'.
getBasicBinCompatibilityMatrix <- function(w){
    ## start with adjacency matrix and remove incompatible eges
    bcm <- w$am
    bcm[bcm>0] <- unlist(lapply(which(bcm>0),function(x){
        from <- row(bcm)[x]
        to <- col(bcm)[x]
        return(any(
            w$ruleList[w$ruleInv[from,]>0,"outBall"] %in%
            w$ruleList[w$ruleInv[to,]>0,"inBall"]
        ))
    }))
    return(bcm)
}

## Does the world have any cycles?
hasCycle <- function(am){
    if(length(am)>1){
    	k <- sum(kcycle.census(am,3,tabulate.by.vertex=FALSE)$cycle.count)
        if(any(k>0)){
            return(k)
        }else{
            return(FALSE)
        }
    }else{
        return(FALSE)
    }
}

## Is the world 'alive'?
worldIsAlive <- function(w,removeSelfPass=TRUE){
    ## first check the input environment
    w$cm <- getCompatibilityMatrix(w)
    w$bcm <- getBinCompatibilityMatrix(w)
    if(w$tick==0){return(TRUE)} ## heuristic, always call a fresh world alive
    if(length(w$cm)<=1){return(FALSE)}
    if(removeSelfPass && !is.null(w$allowSelfPass) && w$allowSelfPass){
        ## if we're allowing self pass, don't count those edges in cycles
        for(i in rownames(w$cm)[duplicated(rownames(w$cm))]){
            w$cm[rownames(w$cm)==i,colnames(w$cm)==i]
        }
    }
    missingBalls <- w$ballEnv<1
    compatRules <- w$ruleList[,"inBall"] %in% which(!missingBalls)

    if(any(missingBalls)){
        if(sum(compatRules)==1){
            if(!any(
                w$cm[w$ruleInv[as.numeric(rownames(w$cm)),compatRules]>0,]>0
            )){
                return(FALSE)
            }
        }else{
            if(!any(
                w$cm[
                    rowSums(w$ruleInv[as.numeric(rownames(w$cm)),
                    compatRules])>0,
                ]>0
            )){
                ## if none of the bins that have a rule compatible with some
                ## input ball have an ouput address, then we call the system dead.
                return(FALSE)
            }
        }
    }
    return(hasCycle(matrix(w$cm)))
}




##################################################
##                  #########                   ##
####        ##########################        ####
##################################################
##   Some generally useful utility functions    ##
##################################################
####        ##########################        ####
##                  #########                   ##
##################################################


## distribute k items over n slots, uniformly or randomly.
## if k not multiple of n, distributes remainder randomly (without repeats).
distribute <- function(k,n,random=FALSE){
    if(random){
    	a <- c(1:n)
    	b <- sample(a,k)
    	m <- matrix(0,100)
    	for (i in b){m[i] <- m[i] + 1}
        res <- as.numeric(m)
    }else{
        res <- rep(floor(k/n),n)
        extras <- sample(n,k-sum(res),replace=FALSE)
        res[extras] <- res[extras] + 1
    }
    return(res)
}


## makes an igraph object with edges determined by moore neighborhoods
agraph.moore <- function(n,nrow=sqrt(n),ncol=sqrt(n)){
    require(igraph)
    ## initialize an agent graph with moore-neighborhood connections
    if(round(nrow)*round(ncol) != n)
        stop("nrow and ncol do not match n. Either they are misspecified or n ",
             "is not a perfect square")
    res <- graph.empty(n)
    v <- seq.int(n)-1
    edgelist <- c(matrix(c(
        v+1,((v %% nrow)+1)%%ncol + floor(v/nrow)*nrow+1, #n me:E [2]
        v+1,((v %% nrow)-1)%%ncol + floor(v/nrow)*nrow+1, #s me:W, but it's not [0 instead of 10]
        v+1,(v-ncol)%%n+1, #e me:N 91
        v+1,(v+ncol)%%n+1, #w me:S 11
        v+1,((v %% nrow)+1)%%nrow + ((floor(v/nrow)*nrow+nrow)%%n)+1, #ne me: SE [12]
        v+1,((v %% nrow)-1)%%nrow + ((floor(v/nrow)*nrow+nrow)%%n)+1, #se me: [10] Should be 20
        v+1,((v %% nrow)+1)%%nrow + ((floor(v/nrow)*nrow-nrow)%%n)+1, #nw me: NE [92]
        v+1,((v %% nrow)-1)%%nrow + ((floor(v/nrow)*nrow-nrow)%%n)+1  #sw me: [90] should be 100
    ),nrow=16,byrow=T))

    res <- add.edges(res,edgelist)
    E(res)$weight <- 1
    res$layout <- cbind(
        c(col(matrix(seq.int(n),nrow=nrow))),
        c(row(matrix(seq.int(n),nrow=nrow)))
    )
    return(res)
}


